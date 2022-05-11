package ru.bvs.dispenser.impl.repo

import akka.Done
import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.github.nscala_time.time.Imports._
import ru.bvs.dispenser.impl.actions.config.Config
import scala.jdk.CollectionConverters.CollectionHasAsScala


class PageParserRepository(repo: CassandraSession)(implicit ac: ActorSystem) extends IPageParserRepository[Future] {

    val logger: Logger = LoggerFactory.getLogger("DAO level")

    override def save(id: String, result: Seq[String]): Future[Done] = {

        if (result.nonEmpty) {

            // преобразуем структуру результата для записи в бд
            val resultDbSet = if (result.length == 1)
                s"'${result.head}'"
            else
                result.map(elem => s"'$elem'").reduce(_ + "," + _)

            logger.info("Вставляем результат запроса в бд")

            for {

                // TODO перенести в миграцию
/*                _ <- repo.executeWrite(
                    "CREATE TABLE IF NOT EXISTS tag_parser ( " +
                        "id TEXT, result SET <TEXT>, PRIMARY KEY (id))"
                )*/

                res <- repo.executeWrite(
                    s"INSERT INTO tag_parser (id, result) VALUES ('$id', {$resultDbSet})" +
                    s"USING TTL ${Config.getTTLRowCache} AND TIMESTAMP ${new DateTime().getMillis * 1000}"
                )

                _ = logger.info(s"Результат инсерта: $res")
            } yield {
                Done
            }

        } else Future.successful(Done)
    }

    override def read(id: String): Future[Seq[String]] = {
        repo.select(s"SELECT id, result FROM tag_parser WHERE id='$id'")
            .runFold(Seq.empty[String]) { case (res, row) =>

                logger.info(s"Считана строка из бд: ${row.getSet[String]("result", classOf[String])}")

                res ++ row.getSet[String]("result", classOf[String]).asScala.toSeq
            }
            .recoverWith { case _ => Future.successful(Seq.empty[String]) }
    }
}

