package de.codecentric
package runnersparadise.interpreters.cassandra

import com.outworkers.phantom.dsl._

object Keyspaces {
  val local: KeySpaceDef = ContactPoint.local.keySpace("runnersparadise")
  val embedded: KeySpaceDef = ContactPoint.embedded.keySpace("embeddedrunnersparadise")
}
