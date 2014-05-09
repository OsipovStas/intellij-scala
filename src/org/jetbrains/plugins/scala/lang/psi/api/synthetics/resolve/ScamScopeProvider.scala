package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.resolve

import com.intellij.psi.ResolveScopeProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.roots.{ProjectRootManager, ProjectFileIndex}
import com.intellij.openapi.module.Module
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.language.ScamFileType

/**
 * @author stasstels
 * @since  5/7/14.
 */
class ScamScopeProvider extends ResolveScopeProvider {

  override def getResolveScope(file: VirtualFile, project: Project): GlobalSearchScope = {

    if (file.getFileType ne ScamFileType) return null
    val projectFileIndex: ProjectFileIndex = ProjectRootManager.getInstance(project).getFileIndex
    val module: Module = projectFileIndex.getModuleForFile(file)
    if (module == null) return null
    val scope = GlobalSearchScope.allScope(project)
    SyntheticResolveUtil.patchedResolveScope(scope)
  }
}
