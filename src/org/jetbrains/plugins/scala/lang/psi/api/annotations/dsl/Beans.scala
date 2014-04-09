package org.jetbrains.plugins.scala
package lang.psi.api.annotations.dsl

import scala.beans.{BooleanBeanProperty, BeanProperty}


/**
 * @author stasstels
 * @since  3/30/14.
 */
object Beans extends MacroAnnotation {

  import MacroAnnotation._


  definitions = Seq(
    holder.asValue.getContainingClass += Methods.getter,
    holder.asVariable.getContainingClass ++ Seq(Methods.getter, Methods.setter)
  )

  annotations = Seq(
    new BeanProperty().getClass.getCanonicalName
  )

}


object BooleanBeans extends MacroAnnotation {

  import MacroAnnotation._


  definitions = Seq(
    holder.asValue.getContainingClass += Methods.isGetter,
    holder.asVariable.getContainingClass ++ Seq(Methods.isGetter, Methods.setter)
  )

  annotations = Seq(
    new BooleanBeanProperty().getClass.getCanonicalName
  )

}