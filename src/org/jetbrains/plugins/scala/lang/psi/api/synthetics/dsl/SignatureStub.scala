package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.dsl.tree.Method
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement

/**
 * @author stasstels
 * @since  5/21/14.
 */
case class SignatureStub(method: Method, reference: ScNamedElement) {
  val name = method.name(reference.getName)
}
