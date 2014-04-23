package org.jetbrains.plugins.scala
package lang.psi.api.annotations.types

import org.jetbrains.plugins.scala.lang.psi.api.base.types._
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.types._
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.types.InfixType
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.types.ParenthesisType
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.types.SimpleType

/**
 * @author stasstels
 * @since  4/17/14.
 */
object Types {

  implicit  def tpe2dsl(tpe: ScTypeElement): Type = tpe match {
    case s: ScSimpleTypeElement => SimpleType(s.calcType.toString)
    case i: ScInfixTypeElement => InfixType(i.ref.toString, tpe2dsl(i.lOp), i.rOp.map(tpe2dsl))
    case p: ScParenthesisedTypeElement => ParenthesisType(p.typeElement.map(tpe2dsl))
    case p: ScParameterizedTypeElement => ParametrizedType(tpe2dsl(p.typeElement), p.typeArgList.typeArgs.map(tpe2dsl))
    case t: ScTupleTypeElement => TupleType(t.components.map(tpe2dsl))
    case c: ScCompoundTypeElement => CompoundType(c.components.map(tpe2dsl))
    case f: ScFunctionalTypeElement => FunctionType(tpe2dsl(f.paramTypeElement), f.returnTypeElement.map(tpe2dsl))
  }

}
