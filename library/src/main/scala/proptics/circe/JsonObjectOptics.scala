// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package proptics.circe

import cats.Applicative
import io.circe.{Json, JsonObject}
import proptics._
import proptics.instances.traverseWithIndex._
import proptics.std.all.some
import proptics.typeclass.{At, Each}

trait JsonObjectOptics {
  final lazy val jsonObjectFields: Fold[JsonObject, (String, Json)] =
    Fold
      .fromFoldable[List, (String, Json)]
      .compose(Fold[JsonObject, List[(String, Json)]](_.toList))

  private[circe] final lazy val indexedJsonObjectFields
      : IndexedTraversal[String, JsonObject, Json] = {
    IndexedTraversal
      .fromTraverseWithIndex[Map[String, *], String, Json]
      .compose(questionableJsonObjectIso)
  }

  private final lazy val questionableJsonObjectIso: Iso[JsonObject, Map[String, Json]] =
    Iso[JsonObject, Map[String, Json]](
      (j: JsonObject) => j.toMap
    )(
      m => JsonObject.fromMap(m)
    )

  implicit final lazy val eachJsonObject: Each[JsonObject, Json] = new Each[JsonObject, Json] {
    override def each: Traversal[JsonObject, Json] =
      Traversal.fromBazaar(bizarreJsonObject.bazaar)
  }

  private final lazy val bizarreJsonObject: Bizarre[Json, JsonObject] =
    new Bizarre[Json, JsonObject] {
      override def runBizarre[F[_]](pafb: Json => F[Json])(s: JsonObject)(implicit
          ev: Applicative[F]
      ): F[JsonObject] = s.traverse(pafb)
    }

  implicit final lazy val atJsonObject: At[JsonObject, String, Json] =
    new At[JsonObject, String, Json] {
      override def at(i: String): Lens[JsonObject, Option[Json]] =
        Lens[JsonObject, Option[Json]](_.apply(i))(
          jso => opt => opt.fold(jso.remove(i))(jso.add(i, _))
        )

      override def ix(i: String): AffineTraversal[JsonObject, Json] = some[Json].compose(at(i))
    }
}

final object JsonObjectOptics extends JsonObjectOptics
