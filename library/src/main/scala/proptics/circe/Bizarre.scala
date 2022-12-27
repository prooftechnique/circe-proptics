// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package proptics.circe

import cats.Applicative
import proptics.internal.{Bazaar, RunBazaar}

trait Bizarre_[A, B, S, T] {
  def runBizarre[F[_]](pafb: A => F[B])(s: S)(implicit ev: Applicative[F]): F[T]

  implicit val bazaar: Bazaar[Function, A, B, S, T] = new Bazaar[Function, A, B, S, T] {
    def runBazaar: RunBazaar[Function, A, B, S, T] = new RunBazaar[Function, A, B, S, T] {
      override def apply[F[_]](pafb: A => F[B])(s: S)(implicit ev: Applicative[F]): F[T] =
        runBizarre(pafb)(s)
    }
  }

}

trait Bizarre[A, B] extends Bizarre_[A, A, B, B]
