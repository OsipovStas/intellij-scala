package org.jetbrains.plugins.scala
package lang.psi.api.annotations.dsl

import scala.beans.BooleanBeanProperty
import scala.annotation.StaticAnnotation


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
    new SyntheticBeanProperty().getClass.getCanonicalName
  )

}

class SyntheticBeanProperty() extends StaticAnnotation

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