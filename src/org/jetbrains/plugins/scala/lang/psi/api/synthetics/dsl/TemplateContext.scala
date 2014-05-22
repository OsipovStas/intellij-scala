package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTemplateDefinition
import org.jetbrains.plugins.scala.dsl.types.ScalaType
import org.jetbrains.plugins.scala.dsl.tree.Template
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil


/**
 * @author stasstels
 * @since  5/18/14.
 */

class TemplateContext(tmpl: ScTemplateDefinition) extends SignatureStubContext {

  implicit private def t2t(st: ScalaType): ScType = SyntheticUtil.ScalaType2ScType(st, tmpl.getContext, tmpl)


  override def addStub(stub: SignatureStub): Unit = {
    _stubs = stub +: _stubs
  }

  private var _stubs: Seq[SignatureStub] = Seq.empty

  override def stubs: Seq[SignatureStub] = _stubs

  override def conform(st1: ScalaType, st2: ScalaType): Boolean = st1 conforms st2

  override def equiv(st1: ScalaType, st2: ScalaType): Boolean = st1 equiv st2

  override def template: Template = PsiReflectTemplate(tmpl, tmpl)
}

