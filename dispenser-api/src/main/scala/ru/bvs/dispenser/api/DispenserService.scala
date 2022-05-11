package ru.bvs.dispenser.api

import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import ru.bvs.dispenser.api.model.{ResultTags, TagPointsData}


trait DispenserService extends Service {


  /**
    * Запрос возвращает список значений заданных тегов с указанного url
    * Полученные результаты кешируются в кассандру и при последующих запросах
    * запрашиваются по ключу из БД. Время жизни записи кэша задается в файле
    * application.conf имя параметра rowCacheTTL (по умолчанию 60 секунд)
    *
    * Запрос методом POST, принимает параметр содержащий:
    * @param urls - путь к странице для парсинга тегов формата http://www.adress.ru
    * @param startTag - открытие целевого тега
    * @param endTag - закрытие целевого тега
    *
    * @return Список значений заданного тега на заданной странице
    *
    * Пример:
    * {{{
    *{
    *  "urls": [
    *  "https://www.lagomframework.com",
    *  "https://www.lagomframework.com/documentation"
    *  ]
    *  "startTag": "<title>",
    *  "endTag": "</title>"
    *}
    * }}}
    */
  def parseTag: ServiceCall[TagPointsData, ResultTags]

  override final def descriptor: Descriptor = {
    import Service._
    named("dispenser")
      .withCalls(
        pathCall("/api/tag-parse", parseTag _)
      )
      .withAutoAcl(true)
  }
}