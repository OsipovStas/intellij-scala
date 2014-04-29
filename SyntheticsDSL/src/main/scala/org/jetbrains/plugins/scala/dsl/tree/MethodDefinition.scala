package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.types.Type
import org.jetbrains.plugins.scala.dsl.TypeContext

/**
 * @author stasstels
 * @since  4/29/14.
 */
case class MethodDefinition(name: String => String,
                           parameters: TypeContext => Seq[Type],
                           returnType: TypeContext => Option[Type])
