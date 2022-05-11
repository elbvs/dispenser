package ru.bvs.dispenser.impl.actions

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding.Get
import org.slf4j.{Logger, LoggerFactory}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import ru.bvs.dispenser.api.model.TagPointData
import ru.bvs.dispenser.impl.actions.utils.TagHelper.TagsToRegExp
import ru.bvs.dispenser.impl.repo.{IPageParserRepository, PageParserRepository}

import scala.concurrent.{ExecutionContext, Future}

class PageParserInterpreter private(repo: IPageParserRepository[Future])(implicit as: ActorSystem) extends PageParserAlgebra[Future] {

    val logger: Logger = LoggerFactory.getLogger("Interpreter level")

    implicit val executor: ExecutionContext = scala.concurrent.ExecutionContext.global

    override def parse(data: TagPointData): Future[Seq[String]] = {

        logger.info(s"Начало парсинга урла $data")

        Http().singleRequest(Get(Uri(data.url))).flatMap { res =>

            val pageCode = res.entity.dataBytes.runFold("") { case (res, byte) =>
                    res + byte.utf8String
            }

            pageCode.map { code =>
                data.toRegexp
                    .findAllMatchIn(code)
                    .map(_.group("body"))
                    .toSeq
            }
        }(executor)
    }

    override def save(data: TagPointData, result: Seq[String]): Future[Done] = repo.save(
        id = data.id,
        result = result
    )

    override def load(id: String): Future[Seq[String]] = repo.read(id)

    override def pure[A](value: A): Future[A] = Future.successful(value)
}

object PageParserInterpreter {

    def make(repo: CassandraSession)(implicit ac: ActorSystem): PageParserInterpreter = {
        val repository = new PageParserRepository(repo)
        new PageParserInterpreter(repository)
    }


}
