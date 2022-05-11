package ru.bvs.dispenser.api.model

import play.api.libs.json.{Format, Json}

/**
  * Класс содержащий информацию из запроса localhost:9000/api/tag-parse
  * @param url - адрес страницы для парсинга (Строка)
  * @param startTag - открывающая конструкция (Строка)
  * @param endTag - закрывающая конструкция (Строка)
  */
case class TagPointData(url: String, startTag: String, endTag: String) {
    val id: String = s"$url.$startTag.$endTag"
}

object TagPointData {
    implicit val format: Format[TagPointData] = Json.format[TagPointData]
}
