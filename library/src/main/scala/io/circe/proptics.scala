// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package io.circe

final object proptics {
  def isNegativeZero(jn: JsonNumber): Boolean =
    jn.toBiggerDecimal.isNegativeZero

  /** So named because it sticks in your brain and hurts like hell.
    */
  final object icepick {
    import io.circe
    type JsonNumber = circe.JsonNumber
    val JsonBigDecimal: circe.JsonBigDecimal.type = circe.JsonBigDecimal
    val JsonLong: circe.JsonLong.type             = circe.JsonLong
    val JsonNumber: circe.JsonNumber.type         = circe.JsonNumber
  }
}
