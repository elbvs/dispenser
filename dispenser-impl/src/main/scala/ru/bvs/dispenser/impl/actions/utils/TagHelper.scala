package ru.bvs.dispenser.impl.actions.utils

import ru.bvs.dispenser.api.model.TagPointData

import scala.util.matching.Regex

case object TagHelper {
    /**
      * Расширение класса getTag методом преобразования стартового тега и конечного в регулярное выражение
      */
    implicit class TagsToRegExp(data: TagPointData) {
        def toRegexp: Regex = {
            // экранируем символы на случай наличия служебных символов RegEx
            val ShieldingTag: String => String =
                tag => tag.foldLeft("") { case (res, char) => res + s"[$char]" }

            val startTag = ShieldingTag(data.startTag)

            val endTag = ShieldingTag(data.endTag)

            // формируем регулярное выражение с именованной группой body которая содержит все что заключено между тегами
            (startTag + """(?<body>[\S\s]*?)""" + endTag).r
        }
    }
}
