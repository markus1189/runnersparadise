import de.codecentric.exp.Transf.P
import de.codecentric.exp.{ExpPrograms, FinalExp, InitialExp, Transf}

ExpPrograms.program[Int]
ExpPrograms.program(Transf.pushNeg[Int])(P)

ExpPrograms.program[String]
ExpPrograms.program(Transf.pushNeg[String])(P)

val initial = ExpPrograms.program[InitialExp]
initial
ExpPrograms.program(Transf.pushNeg[InitialExp])(P)

FinalExp.finalize[String](initial)
FinalExp.finalize[Int](initial)