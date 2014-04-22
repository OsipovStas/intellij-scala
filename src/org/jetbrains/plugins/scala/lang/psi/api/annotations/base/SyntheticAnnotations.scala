package org.jetbrains.plugins.scala
package lang.psi.api.annotations.base

import org.jetbrains.plugins.scala.lang.psi.api.annotations.typedef.SyntheticOwner
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.{BooleanBeans, Beans}
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.MacroAnnotation.SyntheticDefinitions

/**
 * @author stasstels
 * @since  3/30/14.
 */
object SyntheticAnnotations {

  def getCreatorsFor(owner: SyntheticOwner) = generators.filter(_.checkAnnotation(owner)).flatMap(_.getSuitableCreators(owner))

  def register(definitions: Seq[SyntheticDefinitions], annotations: Seq[String]) {
    generators = beanGenerators :+ SyntheticGenerator(definitions, annotations)
  }

  val beanGenerators = Seq(SyntheticGenerator(Beans), SyntheticGenerator(BooleanBeans))

  var generators: Seq[SyntheticGenerator] = beanGenerators

}

