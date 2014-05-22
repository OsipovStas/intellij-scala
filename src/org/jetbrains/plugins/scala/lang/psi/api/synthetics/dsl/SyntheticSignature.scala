package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.types.{Signature, ScSubstitutor}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.dsl.tree.TypedMember
import org.jetbrains.plugins.scala.dsl.types.ScalaType
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil.ScalaType2ScType
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember

/**
  * @author stasstels
 * @since  5/21/14.
 */
case class SyntheticSignature(stub: SignatureStub, subst: ScSubstitutor) extends {
    private val parameters = ScalaPsiUtil.getTypesStream[((TypedMember) => ScalaType)](stub.method.parameters, (t: ((TypedMember) => ScalaType)) => {
      ScalaType2ScType(t(SyntheticUtil.findMember(stub.reference)), stub.reference.getContext, stub.reference)
    })
    private val paramsLength: Int = stub.method.parameters.length
} with Signature(stub.name, parameters, paramsLength, subst, stub.reference) {

  val template = SyntheticUtil.findOwner(stub.reference)

}
