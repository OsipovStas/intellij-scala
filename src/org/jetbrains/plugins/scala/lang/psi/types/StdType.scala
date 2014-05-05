package org.jetbrains.plugins.scala
package lang
package psi
package types

import impl.toplevel.synthetic.{SyntheticClasses, ScSyntheticClass}
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiManager}
import impl.ScalaPsiManager
import extensions.toPsiClassExt
import api.toplevel.typedef.ScObject
import scala.Some
import org.jetbrains.plugins.scala.dsl.types.{StdTypes, ScalaType}


abstract class StdType(val name: String, val tSuper: Option[StdType]) extends ValueType {


  override def asScalaType: ScalaType = super.asScalaType

  def visitType(visitor: ScalaTypeVisitor) {
    visitor.visitStdType(this)
  }

  /**
   * Return wrapped to option appropriate synthetic class.
   * In dumb mode returns None (or before it ends to register classes).
   * @param project in which project to find this class
   * @return If possible class to represent this type.
   */
  def asClass(project: Project): Option[ScSyntheticClass] = {
    if (SyntheticClasses.get(project).isClassesRegistered)
      Some(SyntheticClasses.get(project).byName(name).get)
    else None
  }

  override def equivInner(r: ScType, subst: ScUndefinedSubstitutor, falseUndef: Boolean): (Boolean, ScUndefinedSubstitutor) = {
    (this, r) match {
      case (l: StdType, _: StdType) => (l == r, subst)
      case (AnyRef, _) => {
        ScType.extractClass(r) match {
          case Some(clazz) if clazz.qualifiedName == "java.lang.Object" => (true, subst)
          case _ => (false, subst)
        }
      }
      case (_, _) => {
        ScType.extractClass(r) match {
          case Some(o: ScObject)  => (false, subst)
          case Some(clazz) if clazz.qualifiedName == "scala." + name => (true, subst)
          case _ => (false, subst)
        }
      }
    }
  }

  override def isFinalType = tSuper == Some(AnyVal)
}

object StdType {
  val QualNameToType = Map(
    "scala.Any" -> Any,
    "scala.AnyRef" -> AnyRef,
    "scala.AnyVal" -> AnyVal,
    "scala.Unit" -> Unit,
    "scala.Boolean" -> Boolean,
    "scala.Byte" -> Byte,
    "scala.Short" -> Short,
    "scala.Char" -> Char,
    "scala.Int" -> Int,
    "scala.Long" -> Long,
    "scala.Double" -> Double,
    "scala.Float" -> Float,
    "scala.Null" -> Null,
    "scala.Nothing" -> Nothing,
    "scala.Singleton" -> Singleton
  )

  import com.intellij.psi.CommonClassNames._
  val fqnBoxedToScType = Map(
    JAVA_LANG_BOOLEAN -> Boolean,
    JAVA_LANG_BYTE -> Byte,
    JAVA_LANG_CHARACTER -> Char,
    JAVA_LANG_SHORT -> Short,
    JAVA_LANG_INTEGER -> Int,
    JAVA_LANG_LONG -> Long,
    JAVA_LANG_FLOAT -> Float,
    JAVA_LANG_DOUBLE -> Double
  )

  def unboxedType(tp: ScType): ScType = {
    val name = tp.canonicalText.stripPrefix("_root_.")
    if (fqnBoxedToScType.contains(name)) fqnBoxedToScType(name)
    else tp
  }

  def unapply(tp: StdType): Option[(String, Option[StdType])] = Some(tp.name, tp.tSuper)
}

trait ValueType extends ScType {

  def isValue = true


  def inferValueType: ValueType = this
}

case object Any extends StdType("Any", None)

case object Null extends StdType("Null", Some(AnyRef))

case object AnyRef extends StdType("AnyRef", Some(Any))

case object Nothing extends StdType("Nothing", Some(Any)) {
  override def asScalaType: ScalaType = StdTypes.Nothing_
}

case object Singleton extends StdType("Singleton", Some(AnyRef))

case object AnyVal extends StdType("AnyVal", Some(Any)) {


  override def asScalaType: ScalaType = StdTypes.AnyVal_

  override def getValType: Option[StdType] = Some(this)
}

abstract class ValType(override val name: String) extends StdType(name, Some(AnyVal)) {
  def apply(element: PsiElement): ScType = {
    apply(element.getManager, element.getResolveScope)
  }

  def apply(manager: PsiManager, scope: GlobalSearchScope): ScType = {
    val clazz =
      ScalaPsiManager.instance(manager.getProject).getCachedClass(scope, "scala." + name)
    if (clazz != null) ScDesignatorType(clazz)
    else this
  }

  override def getValType: Option[StdType] = Some(this)
}

object ValType {
  def unapply(tp: ValType): Option[String] = Some(tp.name)
}

object Unit extends ValType("Unit") {
  override def asScalaType: ScalaType = StdTypes.Unit_
}

object Boolean extends ValType("Boolean") {
  override def asScalaType: ScalaType = StdTypes.Boolean_
}

object Char extends ValType("Char") {
  override def asScalaType: ScalaType = StdTypes.Char_
}

object Int extends ValType("Int") {
  override def asScalaType: ScalaType = StdTypes.Int_
}

object Long extends ValType("Long") {
  override def asScalaType: ScalaType = StdTypes.Long_
}

object Float extends ValType("Float") {
  override def asScalaType: ScalaType = StdTypes.Float_
}

object Double extends ValType("Double") {
  override def asScalaType: ScalaType = StdTypes.Double_
}

object Byte extends ValType("Byte") {
  override def asScalaType: ScalaType = StdTypes.Byte_
}

object Short extends ValType("Short") {
  override def asScalaType: ScalaType = StdTypes.Short_
}
