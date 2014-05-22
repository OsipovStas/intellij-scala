package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  5/21/14.
 */
trait SignatureStubContext extends Context {

  def stubs: Seq[SignatureStub]

  def addStub(stub: SignatureStub)
}
