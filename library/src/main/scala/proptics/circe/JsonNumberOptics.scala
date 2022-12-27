// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package proptics.circe

import proptics._
import io.circe.proptics.icepick.{JsonBigDecimal, JsonLong, JsonNumber}
import io.circe.proptics.isNegativeZero

import java.math.{MathContext, BigDecimal => JBigDecimal}

trait JsonNumberOptics {
  final lazy val jsonNumberBigInt: Prism[JsonNumber, BigInt] =
    jsonNumberPrism[BigInt](_.toBigInt)(
      b => JsonBigDecimal(new JBigDecimal(b.underlying, MathContext.UNLIMITED))
    )

  final lazy val jsonNumberLong: Prism[JsonNumber, Long] =
    jsonNumberPrism[Long](_.toLong)(JsonLong.apply)

  final lazy val jsonNumberInt: Prism[JsonNumber, Int] =
    jsonNumberPrism[Int](_.toInt)(
      i => JsonLong(i.toLong)
    )

  final lazy val jsonNumberShort: Prism[JsonNumber, Short] =
    jsonNumberPrism[Short](_.toShort)(
      s => JsonLong(s.toLong)
    )

  final lazy val jsonNumberByte: Prism[JsonNumber, Byte] =
    jsonNumberPrism[Byte](_.toByte)(
      b => JsonLong(b.toLong)
    )

  final lazy val jsonNumberBigDecimal =
    jsonNumberPrism[BigDecimal](_.toBigDecimal)(
      b => JsonBigDecimal(b.underlying)
    )

  private def jsonNumberPrism[A](preview: JsonNumber => Option[A])(
      review: A => JsonNumber
  ): Prism[JsonNumber, A] = Prism.fromPreview[JsonNumber, A] {
    jn =>
      Option.unless(isNegativeZero(jn))(preview(jn)).flatten
  }(review)

}

final object JsonNumberOptics extends JsonNumberOptics
