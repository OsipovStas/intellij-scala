package org.jetbrains.plugins.scala.dsl.base

import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  5/5/14.
 */

abstract class ScamScript {
  def run()(implicit ctx: Context): Unit
}

object ScamScript