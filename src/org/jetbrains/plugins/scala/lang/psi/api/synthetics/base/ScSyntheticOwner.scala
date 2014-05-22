package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.base

import com.intellij.psi.PsiMethod
import scala.annotation.tailrec
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.dsl.{SignatureStub, SyntheticSignature}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait ScSyntheticOwner extends ScalaPsiElement {


  @volatile
  private var sigModCount = 0L

  @volatile
  private var signatures: Seq[SignatureStub] = null

  @volatile
  private var fakeModCount = 0L

  @volatile
  private var fakes: Map[String, PsiMethod] = null


  @tailrec
  final def syntheticStubs: Seq[SignatureStub] = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (signatures != null && sigModCount == curModCount) signatures
    else {
      val res = SyntheticUtil.signatureStubs(this)
      sigModCount = curModCount
      signatures = res
      syntheticStubs
    }
  }

  @tailrec
  final def fake(siga: SyntheticSignature): Option[PsiMethod] = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (fakes != null && fakeModCount == curModCount) {
      fakes.get(siga.name)
    } else {
      val map = syntheticStubs.map {
        case stub => (stub.name, SyntheticUtil.createFake(stub))
      }.toMap
      fakes = map
      fakeModCount = curModCount
      fake(siga)
    }
  }

}
