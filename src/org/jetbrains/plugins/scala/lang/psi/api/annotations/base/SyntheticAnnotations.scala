package org.jetbrains.plugins.scala
package lang.psi.api.annotations.base

import org.jetbrains.plugins.scala.lang.psi.api.annotations.typedef.SyntheticOwner
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.{BooleanBeans, Beans, MacroAnnotation}

/**
 * @author stasstels
 * @since  3/30/14.
 */
object SyntheticAnnotations {

  def getCreatorsFor(owner: SyntheticOwner) = generators.filter(_.checkAnnotation(owner)).flatMap(_.getSuitableCreators(owner))

  def register(macroses: Seq[MacroAnnotation]) {
    generators = beanPropertiesGenerators ++ macroses.map(SyntheticGenerator(_))
  }

  def unplug() = {
    generators = beanPropertiesGenerators
  }

  val beanPropertiesGenerators: Seq[SyntheticGenerator] = Seq(SyntheticGenerator(Beans), SyntheticGenerator(BooleanBeans))

  var generators = beanPropertiesGenerators

}

