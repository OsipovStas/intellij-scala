package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.dsl.tree.{Method, ScalaClass}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTemplateDefinition
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil.SyntheticAnalyzerContext
import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  4/29/14.
 */
case class PsiReflectScalaClass(template: ScTemplateDefinition) extends ScalaClass {
  override def add(m: Method)(implicit ctx: Context): Unit = ctx match {
    case sCtx: SyntheticAnalyzerContext =>
      sCtx.add(m)
    case _ =>
  }
}
