package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.base

import org.jetbrains.plugins.scala.lang.psi.types.Signature
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunction, ScTypeAliasDefinition, ScTypeAlias}
import org.jetbrains.plugins.scala.dsl.types._
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParam
import org.jetbrains.plugins.scala.dsl.types.TypeDefinition
import org.jetbrains.plugins.scala.dsl.types.TypeParameter
import org.jetbrains.plugins.scala.lang.psi.types.TypeAliasSignature
import org.jetbrains.plugins.scala.dsl.types.TypeDeclaration
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScTypedDefinition

/**
 * @author stasstels
 * @since  5/4/14.
 */
object DslTypeUtil {

  def createTypeParameter(tp: ScTypeParam): TypeParameter = {
    val lb = tp.lowerBound.getOrNothing.asScalaType
    val ub = tp.upperBound.getOrAny.asScalaType
    val typeParameters = tp.typeParameters.map(createTypeParameter)
    val vb = tp.viewBound.map(_.asScalaType)
    val cb = tp.contextBound.map(_.asScalaType)
    TypeParameter(tp.variance, tp.name, typeParameters, lb, ub, vb, cb)
  }


  def createDeclaration(siga: TypeAliasSignature) = {
    val ta = ScTypeAlias.getCompoundCopy(siga, siga.ta)
    val typeParameters = ta.typeParameters.map(createTypeParameter)
    ta match {
      case tad: ScTypeAliasDefinition =>
        val alias = tad.aliasedType.getOrAny.asScalaType
        TypeDefinition(ta.name, typeParameters, alias)
      case _ =>
        TypeDeclaration(ta.name, typeParameters, ta.lowerBound.getOrNothing.asScalaType, ta.upperBound.getOrAny.asScalaType)
    }
  }

  def createDeclaration(siga: Signature, rt: ScalaType) = {
    siga.namedElement match {
      case f: ScFunction =>
        Some(Def(f.name, f.paramClauses.clauses.map(_.paramTypes.map(_.asScalaType)).headOption.getOrElse(Seq()), rt))
      case t: ScTypedDefinition =>
        if (t.isVar)
          Some(Var(t.name, rt))
        else if (t.isVal)
          Some(Val(t.name, rt))
        else
          None
      case _ => None
    }

  }

}
