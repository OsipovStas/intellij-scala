package org.jetbrains.plugins.scala
package lang.psi.api.annotations.base

import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.{MacroAnnotation, AnnotationHolder}
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.MacroAnnotation.SyntheticDefinitions
import org.jetbrains.plugins.scala.lang.psi.api.annotations.typedef.SyntheticOwner
import org.jetbrains.plugins.scala.lang.psi.api.annotations.base.SyntheticGenerator.SyntheticCreators
import com.intellij.openapi.util.text

/**
 * @author stasstels
 * @since  4/2/14.
 */
trait SyntheticGenerator {

  val creators: Seq[SyntheticCreators]

  def checkAnnotation(holder: SyntheticOwner): Boolean

  def getSuitableCreators(holder: SyntheticOwner): Seq[FakeCreator]

}

object SyntheticGenerator {

  case class SyntheticCreators(predicate: AnnotationHolder => Boolean, creators: Seq[FakeCreator])


  object SyntheticCreators {
    def apply(defs: SyntheticDefinitions): SyntheticCreators = SyntheticCreators(
      predicate = defs.owner(_).isDefined,
      creators = defs.members.map(FakeCreator(_))
    )

  }

  def apply(macros: MacroAnnotation) = new SyntheticGenerator {

    override val creators: Seq[SyntheticCreators] = macros.definitions.map(SyntheticCreators(_))

    override def checkAnnotation(holder: SyntheticOwner): Boolean = holder.annotations.exists {
      case hAnnot => macros.annotations.exists {
        case checkAnnot =>
          val annotText = hAnnot.typeElement.getText.replace(" ", "")
          text.StringUtil.equals(checkAnnot, annotText)  || text.StringUtil.endsWith(checkAnnot, "." + annotText)
      }
    }

    override def getSuitableCreators(holder: SyntheticOwner): Seq[FakeCreator] = creators.flatMap {
      case SyntheticCreators(p, cs) if p(holder.asAnnotationHolder) => cs
      case _ => Seq.empty
    }
  }

}

