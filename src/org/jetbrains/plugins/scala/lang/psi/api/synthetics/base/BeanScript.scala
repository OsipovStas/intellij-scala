package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.base

import org.jetbrains.plugins.scala.dsl.types.Context
import org.jetbrains.plugins.scala.dsl.base.ScamScript
import org.jetbrains.plugins.scala.dsl.tree.{Annotation, Member, Method}
import org.jetbrains.plugins.scala.dsl.types.StdTypes.Unit_

/**
 * @author stasstels
 * @since  5/5/14.
 */
object BeanScript extends ScamScript {
  override def run()(implicit ctx: Context): Unit = {
    val getter= Method("get" + _.capitalize, Seq(), _.getScalaType)
    val is = Method("is" + _.capitalize, Seq(), _.getScalaType)
    val setter = Method("set" + _.capitalize, Seq(_.getScalaType), Unit_)

    for {
      h <- Member.fromContext
    } {
      if (h.hasAnnotation(Annotation("BeanProperty"))) {
        h.asVariable.containingClass.add(Seq(getter, setter))
        h.asValue.containingClass.add(getter)
      }
      if (h.hasAnnotation(Annotation("BooleanBeanProperty"))) {
        h.asVariable.containingClass.add(Seq(is, setter))
        h.asValue.containingClass.add(is)
      }
    }
  }
}
