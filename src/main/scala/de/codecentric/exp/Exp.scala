package de.codecentric
package exp

sealed trait Exp[A] {
  def lit(x: Int): A
  def neg(e: A): A
  def add(e1: A, e2: A): A
}

object Exp {
  def apply[A]()(implicit E: Exp[A]): Exp[A] = implicitly

  implicit val intInst: Exp[Int] = new Exp[Int] {
    override def lit(x: Int): Int = x

    override def neg(e: Int): Int = -e

    override def add(e1: Int, e2: Int): Int = e1 + e2
  }

  implicit val stringInst: Exp[String] = new Exp[String] {
    override def lit(x: Int): String = x.shows

    override def neg(e: String): String = s"-$e"

    override def add(e1: String, e2: String): String = s"($e1 + $e2)"
  }
}

object Transf {
  sealed trait Ctx
  case object P extends Ctx
  case object N extends Ctx

  def pushNeg[A](implicit E: Exp[A]): Exp[Ctx => A] = new Exp[Ctx => A] {
    override def lit(x: Int): (Ctx) => A = {
      case P => E.lit(x)
      case N => E.neg(E.lit(x))
    }

    override def neg(e: (Ctx) => A): (Ctx) => A = {
      case P => e(N)
      case N => e(P)
    }

    override def add(e1: (Ctx) => A, e2: (Ctx) => A): (Ctx) => A = ctx => E.add(e1(ctx), e2(ctx))
  }
}

object ExpPrograms {
  def program[A](implicit E: Exp[A]): A = {
    import E._

    // 8 + -(1 + 2)
    add(
      lit(8),
      neg(
        add(
          lit(1),
          lit(2)
        )
      )
    )
  }
}

sealed trait InitialExp
case class Lit(x: Int)                         extends InitialExp
case class Neg(e: InitialExp)                  extends InitialExp
case class Add(e1: InitialExp, e2: InitialExp) extends InitialExp

object InitialExp {
  implicit val initial: Exp[InitialExp] = new Exp[InitialExp] {
    override def lit(x: Int): InitialExp = Lit(x)

    override def neg(e: InitialExp): InitialExp = Neg(e)

    override def add(e1: InitialExp, e2: InitialExp): InitialExp = Add(e1, e2)
  }
}

object FinalExp {
  def finalize[A](e: InitialExp)(implicit E: Exp[A]): A = {
    import E._

    e match {
      case Lit(x)      => lit(x)
      case Neg(e1)     => neg(finalize(e1))
      case Add(e1, e2) => add(finalize(e1), finalize(e2))
    }
  }
}
