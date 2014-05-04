package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.base

import org.jetbrains.plugins.scala.dsl.types.ScalaType

/**
 * @author stasstels
 * @since  5/1/14.
 */
trait ScalaTypeLike {

  def asScalaType: ScalaType

}
