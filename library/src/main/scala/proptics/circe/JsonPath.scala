// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package proptics.circe

import io.circe.proptics.icepick.JsonNumber
import io.circe.{Json, JsonObject}
import proptics._
import proptics.circe.JsonObjectOptics._
import proptics.circe.JsonOptics._
import proptics.instances.index._
import proptics.syntax.indexedTraversal._

import scala.language.dynamics

final case class JsonPath(json: AffineTraversal[Json, Json]) extends Dynamic {
  final def `null`: AffineTraversal[Json, Unit]           = withPrism[Unit](_Null)
  final def boolean: AffineTraversal[Json, Boolean]       = withPrism[Boolean](_Boolean)
  final def byte: AffineTraversal[Json, Byte]             = withPrism[Byte](_Byte)
  final def short: AffineTraversal[Json, Short]           = withPrism[Short](_Short)
  final def int: AffineTraversal[Json, Int]               = withPrism[Int](_Int)
  final def long: AffineTraversal[Json, Long]             = withPrism[Long](_Long)
  final def bigInt: AffineTraversal[Json, BigInt]         = withPrism[BigInt](_BigInt)
  final def double: AffineTraversal[Json, Double]         = withPrism[Double](_Double)
  final def bigDecimal: AffineTraversal[Json, BigDecimal] = withPrism[BigDecimal](_BigDecimal)
  final def number: AffineTraversal[Json, JsonNumber]     = withPrism[JsonNumber](_Number)
  final def string: AffineTraversal[Json, String]         = withPrism[String](_String)
  final def arr: AffineTraversal[Json, Vector[Json]]      = withPrism[Vector[Json]](_Array)
  final def obj: AffineTraversal[Json, JsonObject]        = withPrism[JsonObject](_Object)

  private final def withPrism[A](prism: Prism[Json, A]): AffineTraversal[Json, A] =
    prism.compose(json)

  final def at(field: String): AffineTraversal[Json, Option[Json]] =
    atJsonObject.at(field).compose(_Object).compose(json)

  final def selectDynamic(field: String): JsonPath = JsonPath(
    atJsonObject.ix(field).compose(_Object).compose(json)
  )

  final def applyDynamic(field: String)(index: Int): JsonPath = selectDynamic(field).index(index)

  final def apply(i: Int): JsonPath = index(i)

  final def index(i: Int): JsonPath = JsonPath(
    indexVector[Json].ix(i).compose(_Array).compose(json)
  )

  final def each: JsonTraversalPath = JsonTraversalPath(descendants.compose(json))

  final def filterByIndex(p: Int => Boolean): JsonTraversalPath = JsonTraversalPath(
    IndexedTraversal.fromTraverse[Vector, Json].filterByIndex(p).compose(arr).unIndex
  )

  final def filterByField(p: String => Boolean): JsonTraversalPath = JsonTraversalPath(
    indexedJsonObjectFields.filterByIndex(p).compose(obj).unIndex
  )

}

final object JsonPath {
  final val root: JsonPath = JsonPath(AffineTraversal.id[Json])
}

final case class JsonTraversalPath(json: Traversal[Json, Json]) extends Dynamic {
  final def `null`: Traversal[Json, Unit]           = withPrism[Unit](_Null)
  final def boolean: Traversal[Json, Boolean]       = withPrism[Boolean](_Boolean)
  final def byte: Traversal[Json, Byte]             = withPrism[Byte](_Byte)
  final def short: Traversal[Json, Short]           = withPrism[Short](_Short)
  final def int: Traversal[Json, Int]               = withPrism[Int](_Int)
  final def long: Traversal[Json, Long]             = withPrism[Long](_Long)
  final def bigInt: Traversal[Json, BigInt]         = withPrism[BigInt](_BigInt)
  final def double: Traversal[Json, Double]         = withPrism[Double](_Double)
  final def bigDecimal: Traversal[Json, BigDecimal] = withPrism[BigDecimal](_BigDecimal)
  final def number: Traversal[Json, JsonNumber]     = withPrism[JsonNumber](_Number)
  final def string: Traversal[Json, String]         = withPrism[String](_String)
  final def arr: Traversal[Json, Vector[Json]]      = withPrism[Vector[Json]](_Array)
  final def obj: Traversal[Json, JsonObject]        = withPrism[JsonObject](_Object)

  final def at(field: String): Traversal[Json, Option[Json]] =
    atJsonObject.at(field).compose(_Object).compose(json)

  final def selectDynamic(field: String): JsonTraversalPath = JsonTraversalPath(
    atJsonObject.ix(field).compose(_Object).compose(json)
  )

  final def applyDynamic(field: String)(index: Int): JsonTraversalPath =
    selectDynamic(field).index(index)

  final def apply(i: Int): JsonTraversalPath = index(i)

  final def index(i: Int): JsonTraversalPath = JsonTraversalPath(
    indexVector[Json].ix(i).compose(_Array).compose(json)
  )

  final def each: JsonTraversalPath = JsonTraversalPath(
    descendants.compose(json)
  )

  final def filterByIndex(p: Int => Boolean): JsonTraversalPath = JsonTraversalPath(
    IndexedTraversal.fromTraverse[Vector, Json].filterByIndex(p).compose(arr).unIndex
  )

  final def filterByField(p: String => Boolean): JsonTraversalPath = JsonTraversalPath(
    indexedJsonObjectFields.filterByIndex(p).compose(obj).unIndex
  )

  private final def withPrism[A](prism: Prism[Json, A]): Traversal[Json, A] =
    prism.compose(json)

}

final case class JsonFoldPath(json: Fold[Json, Json]) extends Dynamic {
  final def `null`: Fold[Json, Unit]           = withPrism[Unit](_Null)
  final def boolean: Fold[Json, Boolean]       = withPrism[Boolean](_Boolean)
  final def byte: Fold[Json, Byte]             = withPrism[Byte](_Byte)
  final def short: Fold[Json, Short]           = withPrism[Short](_Short)
  final def int: Fold[Json, Int]               = withPrism[Int](_Int)
  final def long: Fold[Json, Long]             = withPrism[Long](_Long)
  final def bigInt: Fold[Json, BigInt]         = withPrism[BigInt](_BigInt)
  final def double: Fold[Json, Double]         = withPrism[Double](_Double)
  final def bigDecimal: Fold[Json, BigDecimal] = withPrism[BigDecimal](_BigDecimal)
  final def number: Fold[Json, JsonNumber]     = withPrism[JsonNumber](_Number)
  final def string: Fold[Json, String]         = withPrism[String](_String)
  final def arr: Fold[Json, Vector[Json]]      = withPrism[Vector[Json]](_Array)
  final def obj: Fold[Json, JsonObject]        = withPrism[JsonObject](_Object)

  final def at(field: String): Fold[Json, Option[Json]] =
    atJsonObject.at(field).compose(_Object).compose(json)

  final def selectDynamic(field: String): JsonFoldPath = JsonFoldPath(
    atJsonObject.ix(field).compose(_Object).compose(json)
  )

  final def applyDynamic(field: String)(index: Int): JsonFoldPath =
    selectDynamic(field).index(index)

  final def apply(i: Int): JsonFoldPath = index(i)

  final def index(i: Int): JsonFoldPath = JsonFoldPath(
    indexVector[Json].ix(i).compose(_Array).compose(json)
  )

  final def each: JsonFoldPath = JsonFoldPath(
    descendants.compose(json)
  )

  final def filterByIndex(p: Int => Boolean): JsonFoldPath = JsonFoldPath(
    IndexedTraversal.fromTraverse[Vector, Json].filterByIndex(p).compose(arr).unIndex
  )

  final def filterByField(p: String => Boolean): JsonFoldPath = JsonFoldPath(
    indexedJsonObjectFields.filterByIndex(p).compose(obj).unIndex
  )

  private final def withPrism[A](prism: Prism[Json, A]): Fold[Json, A] =
    prism.compose(json)

}
