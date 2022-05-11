package ru.bvs.dispenser.impl.repo

import akka.Done


trait IPageParserRepository[F[_]] {

    def save(id: String, result: Seq[String]): F[Done]

    def read(id: String): F[Seq[String]]

}
