package org.jetbrains.plugins.scala
package lang
package psi
package api
package statements

import expr.{ScBlock, ScBlockStatement}
import javax.swing.Icon
import toplevel.templates.ScExtendsBlock
import toplevel.ScTypedDefinition
import org.jetbrains.plugins.scala.lang.psi.types.{Any, ScType}
import toplevel.typedef._
import base.types.ScTypeElement
import icons.Icons
import types.result.{TypingContext, TypeResult}
import com.intellij.psi.PsiElement
import lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.api.annotations.typedef.SyntheticOwner
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.{VarHolder, DefinitionsHolder, AnnotationHolder}

/**
 * @author Alexander Podkhalyuzin
 */

trait ScVariable extends ScBlockStatement with ScMember with ScDocCommentOwner with ScDeclaredElementsHolder with ScAnnotationsHolder with SyntheticOwner {
  self =>
  def varKeyword = findChildrenByType(ScalaTokenTypes.kVAR).apply(0)

  def declaredElements: Seq[ScTypedDefinition]

  def declaredNames: Seq[String] = declaredElements.map(_.name)

  def typeElement: Option[ScTypeElement]

  def declaredType: Option[ScType] = typeElement map (_.getType(TypingContext.empty).getOrAny)

  def getType(ctx: TypingContext): TypeResult[ScType]

  override protected def isSimilarMemberForNavigation(m: ScMember, isStrict: Boolean): Boolean = m match {
    case other: ScVariable =>
      for (elem <- self.declaredElements) {
        if (other.declaredElements.exists(_.name == elem.name))
          return true
      }
      false
    case _ => false
  }
  override def getIcon(flags: Int): Icon = {
    var parent = getParent
    while (parent != null) {
      parent match {
        case _: ScExtendsBlock => return Icons.FIELD_VAR
        case _: ScBlock => return Icons.VAR
        case _ => parent = parent.getParent
      }
    }
    null
  }

  def getVarToken: PsiElement = findFirstChildByType(ScalaTokenTypes.kVAR)

  override def isDeprecated =
    hasAnnotation("scala.deprecated") != None || hasAnnotation("java.lang.Deprecated") != None

  override def asAnnotationHolder: AnnotationHolder =  new VarHolder {

    override def getContainingClass: Option[DefinitionsHolder] = Option(containingClass).map(_ => new DefinitionsHolder {})

    override def getName: String = declaredElements.headOption.map(_.getName).getOrElse("")

    override def getType: String = {

      typeElement.map(_.getText).getOrElse(Any.presentableText)

    }
  }
}