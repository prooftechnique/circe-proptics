// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package proptics.circe

import cats.Applicative
import cats.syntax.all._
import io.circe.{Json, JsonNumber, JsonObject}
import proptics._
import proptics.circe.JsonNumberOptics._
import proptics.circe.JsonObjectOptics._
import proptics.instances.each._
import proptics.std.option.some
import proptics.typeclass.Each

import scala.Function.const

trait JsonOptics {
  final lazy val _Null: Prism[Json, Unit] = Prism[Json, Unit](
    j => Either.cond(j.isNull, (), j)
  )(const(Json.Null))

  final lazy val _Boolean: Prism[Json, Boolean] =
    Prism.fromPreview[Json, Boolean](_.asBoolean)(Json.fromBoolean)

  final lazy val _Number: Prism[Json, JsonNumber] =
    Prism.fromPreview[Json, JsonNumber](_.asNumber)(Json.fromJsonNumber)

  final lazy val _BigDecimal: Prism[Json, BigDecimal] = jsonNumberAt(jsonNumberBigDecimal)
  final lazy val _BigInt: Prism[Json, BigInt]         = jsonNumberAt(jsonNumberBigInt)
  final lazy val _Long: Prism[Json, Long]             = jsonNumberAt(jsonNumberLong)
  final lazy val _Int: Prism[Json, Int]               = jsonNumberAt(jsonNumberInt)
  final lazy val _Short: Prism[Json, Short]           = jsonNumberAt(jsonNumberShort)
  final lazy val _Byte: Prism[Json, Byte]             = jsonNumberAt(jsonNumberByte)

  private final def jsonNumberAt[A](prism: Prism[JsonNumber, A]): Prism[Json, A] =
    prism.compose(_Number)

  final lazy val _String: Prism[Json, String] =
    Prism.fromPreview[Json, String](_.asString)(Json.fromString)
  final lazy val _Object: Prism[Json, JsonObject] =
    Prism.fromPreview[Json, JsonObject](_.asObject)(Json.fromJsonObject)
  final lazy val _Array: Prism[Json, Vector[Json]] =
    Prism.fromPreview[Json, Vector[Json]](_.asArray)(Json.fromValues)

  final lazy val _Double: Prism[Json, Double] =
    Prism.fromPreview[Json, Double] {
      _.foldWith {
        new Json.Folder[Option[Double]] {
          def onNull: Option[Double] = Double.NaN.some

          def onNumber(value: JsonNumber): Option[Double] = {
            val d = value.toDouble
            lazy val roundtrip: Option[Json] => Boolean =
              _Number.compose(some[Json]).contains(value)

            Option.when(!d.isInfinite && roundtrip(Json.fromDouble(d)))(d)
          }

          def onBoolean(value: Boolean): Option[Double]    = none
          def onString(value: String): Option[Double]      = none
          def onArray(value: Vector[Json]): Option[Double] = none
          def onObject(value: JsonObject): Option[Double]  = none
        }
      }
    }(Json.fromDoubleOrNull)

  final lazy val descendants: Traversal[Json, Json] =
    Traversal.fromBazaar(descendantsBizarre.bazaar)

  private final lazy val descendantsBizarre: Bizarre[Json, Json] = new Bizarre[Json, Json] {
    override def runBizarre[F[_]](pafb: Json => F[Json])(s: Json)(implicit
        ev: Applicative[F]
    ): F[Json] = s.fold(
      ev.pure(s),
      const(ev.pure(s)),
      const(ev.pure(s)),
      const(ev.pure(s)),
      arr => ev.map(Each[Vector[Json], Json].each.overF(pafb)(arr))(Json.arr(_: _*)),
      obj => ev.map(Each[JsonObject, Json].each.overF(pafb)(obj))(Json.fromJsonObject)
    )
  }

  implicit final lazy val platedJson: Plated[Json] = new Plated[Json] {
    val plate: Traversal[Json, Json] = Traversal.fromBazaar(platedBizarre.bazaar)
  }

  private final lazy val platedBizarre: Bizarre[Json, Json] =
    new Bizarre[Json, Json] {
      override def runBizarre[F[_]](pafb: Json => F[Json])(s: Json)(implicit
          ev: Applicative[F]
      ): F[Json] = s.fold(
        ev.pure(s),
        b => ev.pure(Json.fromBoolean(b)),
        n => ev.pure(Json.fromJsonNumber(n)),
        s => ev.pure(Json.fromString(s)),
        _.traverse(pafb).map(Json.fromValues),
        _.traverse(pafb).map(Json.fromJsonObject)
      )
    }

}

final object JsonOptics extends JsonOptics
