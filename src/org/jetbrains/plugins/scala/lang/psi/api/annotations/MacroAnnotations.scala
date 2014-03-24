package org.jetbrains.plugins.scala
package lang.psi.api.annotations

import com.intellij.psi.PsiNamedElement
import org.jetbrains.plugins.scala.lang.psi.api.annotations.beans.{BooleanBeanMacroGenerator, BeanMacroGenerator}

/**
 * @author stasstels
 * @since  3/11/14.
 *
 */
object MacroAnnotations {


  def getSyntheticCreatorsFor(definition: PsiNamedElement, noResolve: Boolean = false) = {
    generators.flatMap {
      case gen => gen.getCreatorsFor(definition, noResolve)
    }
  }

  def hasSuitableCreator(baseName: String, decodedName: String) = {
    generators.exists {
      case gen => gen.hasSuitableCreator(baseName, decodedName)
    }
  }

  def getSuitableCreator(baseName: String, decodedName: String) = {
    generators.flatMap {
      case gen => gen.findSuitableCreators(baseName, decodedName)
    }
  }


  val generators: Seq[MacroGenerator] = Seq(BeanMacroGenerator, BooleanBeanMacroGenerator)

}
