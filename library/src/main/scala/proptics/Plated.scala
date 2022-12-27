// SPDX-FileCopyrightText: 2022 Jack Henahan <root@proofte.ch>
//
// SPDX-License-Identifier: Apache-2.0

package proptics

import cats.data.State
import cats.{Applicative, Monad, Traverse}
import cats.syntax.all._
import proptics.circe.Bizarre

abstract class Plated[A] extends Serializable { self =>
  def plate: Traversal[A, A]
}

trait CommonPlatedFunctions {

  /** [[Traversal]] of immediate self-similar children */
  def plate[A](implicit P: Plated[A]): Traversal[A, A] = P.plate
}

trait PlatedFunctions extends CommonPlatedFunctions {

  /** get the immediate self-similar children of a target */
  def children[A: Plated](a: A): List[A] = plate[A].viewAll(a)

  /** rewrite a target by applying a rule as often as possible until it reaches a fixpoint (this is
    * an infinite loop if there is no fixpoint)
    */
  def rewrite[A: Plated](f: A => Option[A])(a: A): A =
    rewriteOf(plate[A].compose(Setter.id[A]))(f)(a)

  /** rewrite a target by applying a rule within a [[Setter]], as often as possible until it reaches
    * a fixpoint (this is an infinite loop if there is no fixpoint)
    */
  def rewriteOf[A](l: Setter[A, A])(f: A => Option[A])(a: A): A = {
    def go: A => A = transformOf(l)(
      x => f(x).map(go).getOrElse(x)
    )
    go(a)
  }

  /** transform every element */
  def transform[A: Plated](f: A => A)(a: A): A =
    transformOf(plate[A].compose(Setter.id[A]))(f)(a)

  /** transform every element by applying a [[Setter]] */
  def transformOf[A](l: Setter[A, A])(f: A => A)(a: A): A = {
    def go: A => A = l
      .over(
        x => go(x)
      )
      .andThen(f)
    go(a)
  }

  /** transforming counting changes */
  def transformCounting[A: Plated](f: A => Option[A])(a: A): (Int, A) =
    transformM[A, State[Int, *]] {
      b =>
        f(b)
          .map(
            c =>
              State(
                (i: Int) => (i + 1, c)
              )
          )
          .getOrElse(State.pure(b))
    }(a).runEmpty.value

  /** transforming every element using monadic transformation */
  def transformM[A: Plated, M[_]: Monad](f: A => M[A])(a: A): M[A] = {
    val l = plate[A]
    def go(c: A): M[A] =
      l.overF[M](
        b => f(b).flatMap(go)
      )(c)
    go(a)
  }

  /** get all transitive self-similar elements of a target, including itself */
  def universe[A: Plated](a: A): LazyList[A] = {
    val fold                  = plate[A].asFold
    def go(b: A): LazyList[A] = b #:: fold.foldMap[LazyList[A]](b)(go)
    go(a)
  }
}

object Plated extends PlatedFunctions {
  def apply[A](traversal: Traversal[A, A]): Plated[A] =
    new Plated[A] {
      override val plate: Traversal[A, A] = traversal
    }

  /** *********************************************************************************************
    */
  /** Std instances */
  /** *********************************************************************************************
    */

  private def listPlatedBizarre[A]: Bizarre[List[A], List[A]] = new Bizarre[List[A], List[A]] {
    override def runBizarre[F[_]](
        pafb: List[A] => F[List[A]]
    )(s: List[A])(implicit ev: Applicative[F]): F[List[A]] =
      s match {
        case x :: xs => Applicative[F].map(pafb(xs))(x :: _)
        case Nil     => Applicative[F].pure(Nil)
      }
  }
  implicit def listPlated[A]: Plated[List[A]] =
    Plated(Traversal.fromBazaar(listPlatedBizarre.bazaar))

  private def lazyListPlatedBizarre[A]: Bizarre[LazyList[A], LazyList[A]] =
    new Bizarre[LazyList[A], LazyList[A]] {
      override def runBizarre[F[_]](
          pafb: LazyList[A] => F[LazyList[A]]
      )(s: LazyList[A])(implicit ev: Applicative[F]): F[LazyList[A]] =
        s match {
          case x #:: xs   => Applicative[F].map(pafb(xs))(x #:: _)
          case LazyList() => Applicative[F].pure(LazyList.empty)
        }
    }

  implicit def lazyListPlated[A]: Plated[LazyList[A]] =
    Plated(Traversal.fromBazaar(lazyListPlatedBizarre.bazaar))

  private def stringPlatedBizarre[A]: Bizarre[String, String] =
    new Bizarre[String, String] {
      override def runBizarre[F[_]](
          pafb: String => F[String]
      )(s: String)(implicit ev: Applicative[F]): F[String] =
        s.headOption match {
          case Some(h) => Applicative[F].map(pafb(s.tail))(h.toString ++ _)
          case None    => Applicative[F].pure("")
        }
    }

  implicit val stringPlated: Plated[String] =
    Plated(Traversal.fromBazaar(stringPlatedBizarre.bazaar))

  private def vectorPlatedBizarre[A]: Bizarre[Vector[A], Vector[A]] =
    new Bizarre[Vector[A], Vector[A]] {
      override def runBizarre[F[_]](
          pafb: Vector[A] => F[Vector[A]]
      )(s: Vector[A])(implicit ev: Applicative[F]): F[Vector[A]] =
        s match {
          case h +: t => Applicative[F].map(pafb(t))(h +: _)
          case _      => Applicative[F].pure(Vector.empty)
        }
    }

  implicit def vectorPlated[A]: Plated[Vector[A]] =
    Plated(Traversal.fromBazaar(vectorPlatedBizarre.bazaar))

  /** *********************************************************************************************
    */
  /** Cats instances */
  /** *********************************************************************************************
    */
  import cats.Now
  import cats.data.Chain
  import cats.free.{Cofree, Free}

  private def chainPlatedBizarre[A]: Bizarre[Chain[A], Chain[A]] =
    new Bizarre[Chain[A], Chain[A]] {
      override def runBizarre[F[_]](
          pafb: Chain[A] => F[Chain[A]]
      )(s: Chain[A])(implicit ev: Applicative[F]): F[Chain[A]] =
        s.uncons match {
          case Some((x, xs)) => Applicative[F].map(pafb(xs))(_.prepend(x))
          case None          => Applicative[F].pure(Chain.empty)
        }
    }

  implicit def chainPlated[A]: Plated[Chain[A]] =
    Plated(Traversal.fromBazaar(chainPlatedBizarre.bazaar))

  private def cofreePlatedBizarre[S[_]: Traverse, A]: Bizarre[Cofree[S, A], Cofree[S, A]] =
    new Bizarre[Cofree[S, A], Cofree[S, A]] {
      override def runBizarre[F[_]](
          pafb: Cofree[S, A] => F[Cofree[S, A]]
      )(s: Cofree[S, A])(implicit ev: Applicative[F]): F[Cofree[S, A]] =
        Applicative[F].map(Traverse[S].traverse(s.tail.value)(pafb))(
          t => Cofree(s.head, Now(t))
        )

    }

  implicit def cofreePlated[S[_]: Traverse, A]: Plated[Cofree[S, A]] =
    Plated(Traversal.fromBazaar(cofreePlatedBizarre.bazaar))

  private def freePlatedBizarre[S[_]: Traverse, A]: Bizarre[Free[S, A], Free[S, A]] =
    new Bizarre[Free[S, A], Free[S, A]] {
      override def runBizarre[F[_]](
          pafb: Free[S, A] => F[Free[S, A]]
      )(s: Free[S, A])(implicit ev: Applicative[F]): F[Free[S, A]] =
        s.resume.fold(
          as => Applicative[F].map(Traverse[S].traverse(as)(pafb))(Free.roll),
          x => Applicative[F].pure(Free.pure(x))
        )
    }

  implicit def freePlated[S[_]: Traverse, A]: Plated[Free[S, A]] =
    Plated(Traversal.fromBazaar(freePlatedBizarre.bazaar))
}
