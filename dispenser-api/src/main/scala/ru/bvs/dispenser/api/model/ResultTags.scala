package ru.bvs.dispenser.api.model

import play.api.libs.json.{Format, Json}

case class ResultTags(success: Map[String, Seq[String]], failure: Map[String, String])

object ResultTags {
    def empty: ResultTags = ResultTags(Map.empty[String, Seq[String]], Map.empty[String, String])
    implicit val format: Format[ResultTags] = Json.format[ResultTags]
}

