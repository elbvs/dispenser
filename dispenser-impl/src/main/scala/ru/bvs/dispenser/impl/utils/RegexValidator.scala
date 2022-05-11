package ru.bvs.dispenser.impl.utils

import scala.util.matching.Regex

case class RegexValidator(s: String, re: Regex, customError: Option[String] = None) extends Validator(customError) {
    override val defaultError = s"Значение должно соответствовать выражению '$re'"
    def isValid: Boolean = re.matches(s)
}
