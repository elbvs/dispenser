package ru.bvs.dispenser.api.model

import play.api.libs.json.{Format, Json}

/**
  * Класс содержащий информацию из запроса localhost:9000/api/tag-parse
  *
  * @param urls - адреса страниц для парсинга (Строка)
  * @param startTag - открывающая конструкция (Строка)
  * @param endTag - закрывающая конструкция (Строка)
  */
case class TagPointsData(urls: Seq[String], startTag: String, endTag: String)

object TagPointsData {
    implicit val format: Format[TagPointsData] = Json.format[TagPointsData]
}

