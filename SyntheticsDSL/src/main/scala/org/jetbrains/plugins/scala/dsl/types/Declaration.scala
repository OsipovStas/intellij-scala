package org.jetbrains.plugins.scala.dsl.types

/**
 * @author stasstels
 * @since  5/3/14.
 */
sealed abstract class Declaration(val kind: String) {

  def show: String

}

case class Def(name: String, params: Seq[ScalaType], rType: ScalaType) extends Declaration("def") {
  override def show: String = {
    val paramText = params.zipWithIndex.map {
      case (st, i) =>
        val s = st.show
        s"arg$i: $s"
    }.mkString("(", ",", ")")
    val rText = rType.show
    s"$kind $name$paramText: $rText"
  }
}

case class Val(name: String, rt: ScalaType) extends Declaration("val") {
  override def show: String = s"$kind $name: ${rt.show}"
}

case class Var(name: String, rt: ScalaType) extends Declaration("var") {
  override def show: String = s"$kind $name: ${rt.show}"
}

case class TypeDeclaration(name: String, params: Seq[TypeParameter], lower: ScalaType = StdTypes.Nothing_, upper: ScalaType = StdTypes.Any_) extends Declaration("type") {
  override def show: String = {
    val paramText = if (params.nonEmpty) params.map(_.show).mkString("[", ",", "]") else ""
    s"$kind $name$paramText >: ${lower.show}  <: ${upper.show}"
  }
}

case class TypeDefinition(name: String, params: Seq[TypeParameter], tpe: ScalaType) extends Declaration("type") {
  override def show: String = {
    val paramText = if (params.nonEmpty) params.map(_.show).mkString("[", ",", "]") else ""
    s"$kind $name$paramText = ${tpe.show}"
  }
}


case class TypeParameter(cov: Int, name: String, params: Seq[TypeParameter], lower: ScalaType, upper: ScalaType, viewBound: Seq[ScalaType], contextBound: Seq[ScalaType]) {
  val symb = cov match {
    case i if i > 0 => "+"
    case i if i < 0 => "-"
    case _ => ""
  }

  def show: String = {
    val lb = s" >: ${lower.show} "
    val ub = s" <: ${upper.show} "
    val vb = viewBound.map(" <% " + _.show).mkString(" ")
    val cb = contextBound.map(" : " + _.show).mkString(" ")
    val paramText = if (params.nonEmpty) params.map(_.show).mkString("[", ",", "]") else ""
    s"$symb $name$paramText $lb $ub $vb $cb"
  }
}

case class Refinement(decls: Seq[Declaration]) {
  def show: String = {
    decls.map(_.show).mkString(" {", " ; ", "} ")
  }
}