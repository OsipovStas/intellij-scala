package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.base

import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.scala.lang.psi.types._
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import scala.annotation.tailrec
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil.SyntheticSignature
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScDeclaredElementsHolder

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait ScSyntheticOwner extends ScMember with ScDeclaredElementsHolder {


  @volatile
  private var sigModCount = 0L

  @volatile
  private var signatures: Seq[SyntheticSignature] = null

  @volatile
  private var fakeModCount = 0L

  @volatile
  private var fakes: Map[String, PsiMethod] = null


  @tailrec
  final def getSyntheticSignatures: Seq[SyntheticSignature] = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (signatures != null && sigModCount == curModCount) signatures
    else {
      val res = SyntheticUtil.getSignaturesFor(this, ScSubstitutor.empty)
      sigModCount = curModCount
      signatures = res
      getSyntheticSignatures
    }
  }

  @tailrec
  final def getFake(siga: SyntheticSignature): Option[PsiMethod] = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (fakes != null && fakeModCount == curModCount) {
      fakes.get(siga.name)
    } else {
      val map = getSyntheticSignatures.map {
        case s => (s.name, SyntheticUtil.createFake(s))
      }.toMap
      fakes = map
      fakeModCount = curModCount
      getFake(siga)
    }
  }

  def name = declaredElements.headOption.fold("")(_.getName)

}
