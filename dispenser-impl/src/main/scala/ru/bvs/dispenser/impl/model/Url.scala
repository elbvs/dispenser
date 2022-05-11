package ru.bvs.dispenser.impl.model

import play.api.libs.json.{Format, Json}

case class Url(value: String) extends AnyVal

object Url {
    implicit val format: Format[Url] = Json.format
}
