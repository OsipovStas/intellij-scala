package org.jetbrains.plugins.scala
package lang.psi.api.annotations.base

import org.jetbrains.plugins.scala.lang.psi.api.annotations.typedef.SyntheticOwner
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.{MacroAnnotation, Type}

/**
 * @author stasstels
 * @since  3/30/14.
 */
object SyntheticAnnotations {

  def getCreatorsFor(owner: SyntheticOwner) = generators.filter(_.checkAnnotation(owner)).flatMap(_.getSuitableCreators(owner))

  def register(macros: MacroAnnotation) {
    generators = generators :+ SyntheticGenerator(macros)
  }

  var generators: Seq[SyntheticGenerator] = Seq()

  case class ProxyType(typeText: String) extends Type
}

