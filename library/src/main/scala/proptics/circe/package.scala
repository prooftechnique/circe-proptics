// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package proptics

import io.circe.{Decoder, Encoder}

package object circe {

  /** Derives a [[io.circe.Decoder]][B] from a Decoder[A] and [[proptics.Iso]][A, B].
    *
    * @tparam A
    *   the type parameter of the existent Decoder.
    * @tparam B
    *   the type parameter of the derived Decoder.
    * @param decoder
    *   the existent Decoder.
    * @param iso
    *   the existent Iso.
    */
  final def deriveDecoderWithIso[A, B](implicit decoder: Decoder[A], iso: Iso[A, B]): Decoder[B] =
    decoder.map(iso.view)

  /** Derives an [[io.circe.Encoder]][B] from an Encoder[A] and [[proptics.Iso]][A, B].
    *
    * @tparam A
    *   the type parameter of the existent Encoder.
    * @tparam B
    *   the type parameter of the derived Encoder.
    * @param encoder
    *   the existent Encoder.
    * @param iso
    *   the existent Iso.
    */
  final def deriveEncoderWithIso[A, B](implicit encoder: Encoder[A], iso: Iso[A, B]): Encoder[B] =
    encoder.contramap(iso.review)

  /** Derives a [[io.circe.Decoder]][B] from a Decoder[A] and [[proptics.Iso]][B, A].
    *
    * @tparam A
    *   the type parameter of the existent Decoder.
    * @tparam B
    *   the type parameter of the derived Decoder.
    * @param decoder
    *   the existent Decoder.
    * @param iso
    *   the existent Iso.
    */
  final def deriveDecoderWithIsoReverse[B, A](implicit
      decoder: Decoder[A],
      iso: Iso[B, A]
  ): Decoder[B] = decoder.map(iso.review)

  /** Derives an [[io.circe.Encoder]][B] from an Encoder[A] and [[proptics.Iso]][B, A].
    *
    * @tparam A
    *   the type parameter of the existent Encoder.
    * @tparam B
    *   the type parameter of the derived Encoder.
    * @param encoder
    *   the existent Encoder.
    * @param iso
    *   the existent Iso.
    */
  final def deriveEncoderWithIsoReverse[B, A](implicit
      encoder: Encoder[A],
      iso: Iso[B, A]
  ): Encoder[B] = encoder.contramap(iso.view)
}
