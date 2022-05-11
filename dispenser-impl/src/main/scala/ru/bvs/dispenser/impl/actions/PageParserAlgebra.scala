package ru.bvs.dispenser.impl.actions

import akka.Done
import ru.bvs.dispenser.api.model.TagPointData

trait PageParserAlgebra[F[_]] {
    def parse(data: TagPointData): F[Seq[String]]

    def save(data: TagPointData, result: Seq[String]): F[Done]

    def load(id: String): F[Seq[String]]

    def pure[A](value: A): F[A]
}
