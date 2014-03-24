package org.jetbrains.plugins.scala
package lang.psi.api.annotations

import com.intellij.psi.PsiNamedElement
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScAnnotationsHolder
import com.intellij.openapi.util.text.StringUtil


/**
 * @author stasstels
 * @since  3/19/14.
 */
trait MacroGenerator {

  def checkAnnotation(s: ScAnnotationsHolder, noResolve: Boolean = false): Boolean

  def hasSuitableCreator(baseName: String, decodedName: String): Boolean = {
    creators.exists {
      case creator => StringUtil.equals(creator.transformedName(baseName), decodedName)
    }
  }

  def findSuitableCreators(baseName: String, decodedName: String): Seq[SyntheticMemberCreator] = {
    creators.filter {
      case creator => StringUtil.equals(creator.transformedName(baseName), decodedName)
    }
  }


  def getCreatorsFor(definition: PsiNamedElement, noResolve: Boolean = false): Seq[SyntheticMemberCreator]
  

  protected val creators: Seq[SyntheticMemberCreator]
}



