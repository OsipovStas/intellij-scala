package org.jetbrains.plugins.scala
package lang.psi.api.synthetics

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTemplateDefinition, ScMember}
import org.jetbrains.plugins.scala.dsl.tree.{Empty, Member, TypedMember}
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.dsl.types.ScalaType
import com.intellij.psi.{PsiNamedElement, PsiManager, PsiElement, PsiMethod}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScVariable, ScValue}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.dsl._
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.base.ScSyntheticOwner
import org.jetbrains.plugins.scala.dsl.base.ScamScript
import org.apache.commons.io.FilenameUtils
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.psi.search.{GlobalSearchScope, FileTypeIndex}
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.language.{SyntheticsProjectComponent, ScamFileType}
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.dsl.PsiReflectValue
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.dsl.PsiReflectVariable
import extensions.toSeqExt
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement

/**
 * @author stasstels
 * @since  4/29/14.
 */
object SyntheticUtil {


  def signatureStubs(owner: ScSyntheticOwner): Seq[SignatureStub] = {
    val scripts = SyntheticsProjectComponent.getInstance(owner.getProject).scripts
    stubContexts(owner).flatMap {
      case ctx =>
        scripts.foreach(_.run()(ctx))
        ctx.stubs
    }
  }

  def signaturesStubsBy(ref: PsiNamedElement): Seq[SignatureStub] = {
    findOwner(ref).toSeq.flatMap {
      case owner => owner.syntheticStubs.filter(stub => stub.reference.equals(ref))
    }
  }

  def stubContexts(owner: ScSyntheticOwner): Seq[SignatureStubContext] = owner match {
    case obj: ScObject => Seq(new TemplateContext(obj))
    case template: ScTemplateDefinition => Seq(new TemplateContext(template))
    case _ => Seq.empty
  }


  def getScamPsiFile(script: VirtualFile, p: Project) = {
    Option(PsiManager.getInstance(p).findFile(script))
  }

  def register(script: ScamScript, p: Project): Unit = {
    val component = SyntheticsProjectComponent.getInstance(p)
    component.register(script)
  }

  def unplug(p: Project) {
    SyntheticsProjectComponent.getInstance(p).unplug()
  }


  def findScamScripts(p: Project): Seq[VirtualFile] = {
    import scala.collection.JavaConversions._
    val searchScope = GlobalSearchScope.allScope(p)
    FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, ScamFileType, searchScope).toSeq
  }

  def scamName(file: VirtualFile) = FilenameUtils.removeExtension(file.getName) + DSL.SCAM_MARKER

  def scamName(file: ScalaFile) = FilenameUtils.removeExtension(file.getName) + DSL.SCAM_MARKER

  def isPluged(file: VirtualFile, p: Project) = {
    SyntheticsProjectComponent.getInstance(p).isPluged(file)
  }

  def ScalaType2ScType(st: ScalaType, context: PsiElement, child: PsiElement) = ScalaPsiElementFactory.createTypeFromText(st.show, context, child)


  def scMember2member(m: ScMember): Member = m match {
    case aVal: ScValue => PsiReflectValue(aVal)
    case aVar: ScVariable => PsiReflectVariable(aVar)
    case _ => Empty
  }


  implicit def arr2arr(a: Array[ScType]): Array[Parameter] = a.toSeq.mapWithIndex {
    case (tpe, index) => new Parameter("", None, tpe, false, false, false, index)
  }.toArray



  def createFake(stub: SignatureStub): PsiMethod = {
    ScamFakeMethod(stub)
  }


  def findMember(reference: ScNamedElement): TypedMember = ScalaPsiUtil.nameContext(reference) match {
    case m: ScMember => PsiReflectMember(m)
    case _ => Empty
  }

  def findOwner(ref: PsiNamedElement): Option[ScSyntheticOwner] = ScalaPsiUtil.nameContext(ref) match {
    case member: ScMember => Option(member.containingClass)
    case _ => None
  }
}
