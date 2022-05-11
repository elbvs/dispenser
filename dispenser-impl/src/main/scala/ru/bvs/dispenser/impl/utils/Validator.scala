package ru.bvs.dispenser.impl.utils

import scala.concurrent.Future

abstract class Validator(customError: Option[String] = None) {
    val defaultError: String = "Значение некорректно"
    def error: String = customError.getOrElse(defaultError)
    def validate: Option[String] = if (isValid) None else Some(error)
    def isValid: Boolean
    def isInvalid: Boolean = !isValid
}

abstract class FormValidator {

    def validators: Map[String, Seq[Validator]]

    def validate: Map[String, String] = {
        validators.flatMap { case (f, vs) =>
            findFirstError(vs).map(f -> _)
        }
    }

    def validateFields(fieldNames: List[String]): Map[String, String] = {
        if (fieldNames.isEmpty) Map.empty
        else fieldNames.flatMap(f => validateField(f).map(f -> _)).toMap
    }

    def validateField(fieldName: String): Option[String] = {
        validators.get(fieldName).flatMap(findFirstError)
    }

    protected def findFirstError(vs: Seq[Validator]): Option[String] = {
        vs.collectFirst { case v if v.isInvalid => v.error }
    }
}

trait Validation {
    protected def validate(v: FormValidator): Future[String] = {
        val res = v.validate
        if(res.nonEmpty) Future.failed(ValidationException(res))
        else Future.successful("")
    }
}
