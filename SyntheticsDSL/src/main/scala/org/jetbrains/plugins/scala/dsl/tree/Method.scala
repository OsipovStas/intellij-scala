package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.types.Type

/**
 * @author stasstels
 * @since  4/29/14.
 */
case class Method(name: String => String,
                  parameters: Seq[Type],
                  returnType: Type)

