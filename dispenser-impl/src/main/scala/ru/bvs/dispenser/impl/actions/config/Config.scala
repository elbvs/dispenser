package ru.bvs.dispenser.impl.actions.config

import com.typesafe.config.ConfigFactory
import scala.util.Try

case object Config {

    private val config = ConfigFactory.load()

    def getTTLRowCache: Int = Try(
        config.getInt("rowCacheTTL")
    ).getOrElse(60)

}
