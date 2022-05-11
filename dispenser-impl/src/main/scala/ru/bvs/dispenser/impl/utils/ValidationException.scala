package ru.bvs.dispenser.impl.utils

case class ValidationException(errors: Map[String, String])
    extends RuntimeException(s"Ошибка валидации: ${
        errors
            .map { case (msg, err) =>
                msg + " - " + err
            }
            .reduce(_ + "\n" + _)
        }"
    )