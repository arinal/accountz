package io.lamedh

import cats.data._
import java.util.Calendar

object common {
  type Amount = BigDecimal
  type ValidationResult[A] = ValidatedNec[String, A]
  type ErrorOr[A] = Either[NonEmptyChain[String], A]

  def today = Calendar.getInstance.getTime
}
