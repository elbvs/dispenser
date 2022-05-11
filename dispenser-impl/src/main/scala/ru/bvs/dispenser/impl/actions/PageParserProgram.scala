package ru.bvs.dispenser.impl.actions

import cats.Monad
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import org.slf4j.{Logger, LoggerFactory}
import ru.bvs.dispenser.api.model.TagPointData

case class PageParserProgram[F[_] : Monad](parser: PageParserAlgebra[F]) {

    val logger: Logger = LoggerFactory.getLogger("Program level")

    def parsePageFromUrl(data: TagPointData): F[Seq[String]] = {
        for {
            cache <- parser.load(data.id)

            res <- if (cache.nonEmpty) parser.pure(cache) else
                for {
                    tags <- parser.parse(data)
                    _ <- parser.save(data, tags)
                } yield tags
        } yield res
    }
}
