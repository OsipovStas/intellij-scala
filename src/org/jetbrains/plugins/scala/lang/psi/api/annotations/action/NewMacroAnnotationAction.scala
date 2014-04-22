package org.jetbrains.plugins.scala
package lang.psi.api.annotations.action

import com.intellij.ide.fileTemplates.actions.CreateFromTemplateAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.actionSystem.{LangDataKeys, AnActionEvent}
import com.intellij.openapi.module.Module
import org.jetbrains.plugins.scala.config.ScalaFacet
import org.jetbrains.plugins.scala.icons.Icons

/**
 * @author stasstels
 * @since  4/20/14.
 */
class NewMacroAnnotationAction extends CreateFromTemplateAction(FileTemplateManager.getInstance().getInternalTemplate("Scala Macro Annotation"))
with DumbAware {
  override def update(e: AnActionEvent) {
    super.update(e)
    val module: Module = e.getDataContext.getData(LangDataKeys.MODULE.getName).asInstanceOf[Module]
    val isEnabled: Boolean = if (module == null) false else ScalaFacet.isPresentIn(module)
    e.getPresentation.setEnabled(isEnabled)
    e.getPresentation.setVisible(isEnabled)
    e.getPresentation.setIcon(Icons.SCRIPT_FILE_LOGO)
  }
}
