package de

import java.util.UUID

import cats.Functor.ToFunctorOps

import scalaz.Show
import scalaz.syntax.{ToEitherOps, ToEqualOps, ToShowOps}

package object codecentric extends ScalazSyntax {
  type Traversable[+A] = scala.collection.immutable.Traversable[A]
  val Traversable = scala.collection.immutable.Traversable

  type Iterable[+A] = scala.collection.immutable.Iterable[A]
  val Iterable = scala.collection.immutable.Iterable

  type Seq[+A] = scala.collection.immutable.Seq[A]
  val Seq = scala.collection.immutable.Seq
  val Nil = scala.collection.immutable.Nil

  type SortedMap[A, +B] = scala.collection.immutable.SortedMap[A, B]
  val SortedMap = scala.collection.immutable.SortedMap

  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]
  val IndexedSeq = scala.collection.immutable.IndexedSeq
}

trait ScalazSyntax extends ToEqualOps with ToShowOps with ToEitherOps with ToFunctorOps {
  implicit val uuidShow: Show[UUID] = Show.showFromToString
  implicit val intShow: Show[Int] = Show.showFromToString
}
