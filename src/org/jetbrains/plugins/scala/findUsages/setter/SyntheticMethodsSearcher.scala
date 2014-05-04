package org.jetbrains.plugins.scala
package findUsages.setter

import com.intellij.util.{Processor, QueryExecutor}
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScVariable, ScFunction}
import com.intellij.psi.search.{UsageSearchContext, PsiSearchHelper, TextOccurenceProcessor, SearchScope}
import org.jetbrains.plugins.scala.lang.psi.fake.FakePsiMethod
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScTypedDefinition
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.base.ScSyntheticOwner

/**
 * @author stasstels
 * @since  4/26/14.
 */

class SyntheticMethodsSearcher extends QueryExecutor[PsiReference, ReferencesSearch.SearchParameters] {
  private val suffixScala = "test$$"

  def execute(queryParameters: ReferencesSearch.SearchParameters, cons: Processor[PsiReference]): Boolean = {
    inReadAction {
      implicit val scope = queryParameters.getEffectiveSearchScope
      implicit val consumer = cons
      val element = queryParameters.getElementToSearch
      if (element.isValid) {
        element match {
          case typed: ScTypedDefinition =>
            typed.nameContext match {
              case owner: ScSyntheticOwner =>
                owner.getSyntheticSignatures.foreach {
                  case siga => processSimpleUsages(typed, siga.name)
                }
              case _ =>
            }
          case _ =>
        }

        element match {
          case fun: ScFunction if fun.name endsWith suffixScala =>
            processSimpleUsages(fun, fun.name)
          case refPattern: ScReferencePattern if ScalaPsiUtil.nameContext(refPattern).isInstanceOf[ScVariable] =>
            val name = refPattern.name
            processSimpleUsages(refPattern, name + suffixScala)
          case _ =>
        }
      }
    }
    true

  }


  private def processSimpleUsages(element: PsiElement, name: String)(implicit consumer: Processor[PsiReference], scope: SearchScope) = {
    val processor = new TextOccurenceProcessor {
      def execute(elem: PsiElement, offsetInElement: Int): Boolean = {
        elem match {
          case ref: PsiReference => ref.resolve() match {
            case fakeMethod: FakePsiMethod if fakeMethod.navElement == element =>
              if (!consumer.process(ref)) return false
            case wrapper: PsiTypedDefinitionWrapper if wrapper.typedDefinition == element =>
              if (!consumer.process(ref)) return false
            case _ =>
          }
          case _ =>
        }
        true
      }
    }
    val helper: PsiSearchHelper = PsiSearchHelper.SERVICE.getInstance(element.getProject)
    helper.processElementsWithWord(processor, scope, name, UsageSearchContext.IN_CODE, true)
  }
}
