package ru.bvs.dispenser.impl

import ru.bvs.dispenser.api.DispenserService
import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import scala.concurrent.{ExecutionContext, Future}
import ru.bvs.dispenser.impl.utils.Validation
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import org.slf4j.{Logger, LoggerFactory}
import ru.bvs.dispenser.api.model.{ResultTags, TagPointData, TagPointsData}
import ru.bvs.dispenser.impl.DispenserServiceImpl.RequestValidator
import ru.bvs.dispenser.impl.actions.{PageParserInterpreter, PageParserProgram}
import ru.bvs.dispenser.impl.utils.{FormValidator, RegexValidator, Validator}


class DispenserServiceImpl(clusterSharding: ClusterSharding,
                           persistentEntityRegistry: PersistentEntityRegistry,
                           implicit val system: ActorSystem,
                           repo: CassandraSession)
                          (implicit ec: ExecutionContext) extends DispenserService with Validation {

    val logger: Logger = LoggerFactory.getLogger("Service level")

    implicit val interpreter: PageParserInterpreter = PageParserInterpreter.make(repo)(system)

    override def parseTag: ServiceCall[TagPointsData, ResultTags] = ServiceCall { case TagPointsData(urls, startTag, endTag) =>

        logger.info(s"Поступил запрос парсинга тегов с урла $urls".take(128))

        Source(urls).mapAsync(50) { url =>

            val tagPointData = TagPointData(url, startTag, endTag)

            for {
                errorMessage <- validate(new RequestValidator(tagPointData)).recover(_.getMessage)

                res <- if (errorMessage.nonEmpty)
                    Future.successful((s"error $url", Seq(errorMessage)))
                else
                    PageParserProgram(interpreter)
                        .parsePageFromUrl(tagPointData)
                        .map(result => (url, result))
                        .recover {
                            case err => (s"error $url", Seq(err.getMessage))
                        }
            } yield res

        }.runFold(ResultTags.empty) { (res, elem) =>
            elem match {
                case (url, tags) if url.take(5) == "error" => res.copy(
                    failure = res.failure ++ Map(url -> tags.headOption.getOrElse(""))
                )
                case (url, tags) => res.copy(
                    success = res.success ++ Map(url -> tags)
                )
            }
        }.recoverWith {
            case err => throw BadRequest(s"Ошибка запроса: ${err.getMessage}")
        }
    }
}

object DispenserServiceImpl {
    class RequestValidator(p: TagPointData) extends FormValidator {
        override def validators: Map[String, Seq[Validator]] = Map(
            "url" -> List(
                RegexValidator(
                    p.url, """[hH][tT][tT][pP][Ss]?://[\s\S]+""".r,
                    Some("Некорректный ввод. Пример верного ввода: https://www.google.ru")
                )
            )
        )
    }
}