package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScModifierListOwner
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.{DSL, SyntheticUtil}
import SyntheticUtil.{ScalaType2ScType, arr2arr}
import org.jetbrains.plugins.scala.lang.psi.fake.FakePsiMethod
import javax.swing.Icon

/**
 * @author stasstels
 * @since  5/21/14.
 */
case class ScamFakeMethod(stub: SignatureStub) extends {
  private val hasModifierProperty: String => Boolean = stub.reference match {
    case v: ScModifierListOwner => v.hasModifierProperty
    case _ => _ => false
  }
  private val typed = SyntheticUtil.findMember(stub.reference)
  private val rType = ScalaType2ScType(stub.method.returnType(typed), stub.reference.getContext, stub.reference)
  private val types = stub.method.parameters.map {
    case t => ScalaType2ScType(t(typed), stub.reference.getContext, stub.reference)
  }.toArray
} with FakePsiMethod(stub.reference, stub.name, types, rType, hasModifierProperty) {
  override def getIcon(flags: Int): Icon = DSL.fakeMethodIcon
}
