package org.jetbrains.plugins.scala
package lang.psi.api.annotations

import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import com.intellij.psi.PsiNamedElement
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.lang.psi.types.ScSubstitutor
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScAnnotationsHolder
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter

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
    }.headOption
  }


  val generators: Seq[Generator] = Seq(BeanGenerator)

}
