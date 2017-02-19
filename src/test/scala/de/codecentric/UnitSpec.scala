package de.codecentric

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, OptionValues, WordSpec}

trait UnitSpec extends WordSpec with Matchers with TypeCheckedTripleEquals with OptionValues
