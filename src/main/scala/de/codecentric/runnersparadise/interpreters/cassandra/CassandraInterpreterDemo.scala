package de.codecentric.runnersparadise.interpreters.cassandra

import de.codecentric.runnersparadise.programs.Programs

import scalaz.concurrent.Task

object CassandraInterpreterDemo extends App {
  val interpreters = new CassandraInterpreter
  import interpreters._
  Programs.demo[Task].run

  LocalDatabase.shutdown()
}