package de.codecentric.runnersparadise.interpreters.cassandra

import com.datastax.driver.core.{ResultSet, Row, Session}
import de.codecentric.runnersparadise.algebra.RunnerAlg
import de.codecentric.runnersparadise.domain.{Runner, RunnerId}

import scala.collection.JavaConverters._
import scalaz.{Kleisli, ReaderT}
import scalaz.syntax.functor._
import scalaz.concurrent.Task

class CassandraRunner(session: Session) extends RunnerAlg[ReaderT[Task, Session, ?]] {
  import CassandraRunner._

  override def saveRunner(runner: Runner): ReaderT[Task, Session, Unit] = {
    ReaderT[Task, Session, ResultSet](
      session =>
        Task.delay(
          session.execute(
            s"insert into $table (id,firstname,lastname,nickname) values (" +
              s"${runner.id.value}," +
              s"${runner.firstname}," +
              s"${runner.lastname}," +
              s"${runner.nickname.orNull}" +
              s")"))).void
  }

  override def findRunner(id: RunnerId): ReaderT[Task, Session, Option[Runner]] = {
    for {
      rowOpt <- ReaderT[Task, Session, Option[Row]](
        session =>
          Task.delay(
            session
              .execute(s"select * from $table where id = '${id.value}'")
              .all()
              .asScala
              .headOption))
    } yield {
      rowOpt.map { row =>
        Runner(
          RunnerId(row.getString("id")),
          row.getString("firstname"),
          row.getString("lastname"),
          Option(row.getString("nickname"))
        )
      }
    }
  }
}

object CassandraRunner {
  val table: String = "runners"
}
