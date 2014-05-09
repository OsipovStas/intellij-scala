package org.jetbrains.plugins.scala.finder

import org.jetbrains.plugins.scala.ScalaFileType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.module.Module
import org.jetbrains.plugins.scala.util.ScalaLanguageDerivative
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.resolve.SyntheticResolveUtil

/**
 * User: Alexander Podkhalyuzin
 * Date: 17.02.2010
 */
class ScalaSourceFilterScope(myDelegate: GlobalSearchScope, project: Project) extends GlobalSearchScope(project) {
  val myIndex = ProjectRootManager.getInstance(project).getFileIndex

  def isSearchInLibraries: Boolean = {
    null == myDelegate || myDelegate.isSearchInLibraries
  }

  def compare(file1: VirtualFile, file2: VirtualFile): Int = {
    if (null != myDelegate) myDelegate.compare(file1, file2) else 0
  }

  def isSearchInModuleContent(aModule: Module): Boolean = {
    null == myDelegate || myDelegate.isSearchInModuleContent(aModule)
  }

  def contains(file: VirtualFile): Boolean = {
    val extention = file.getExtension
    (null == myDelegate || myDelegate.contains(file)) && (
      (ScalaFileType.SCALA_FILE_TYPE.getDefaultExtension == extention || 
        ScalaLanguageDerivative.hasDerivativeForFileType(file.getFileType)) && myIndex.isInSourceContent(file) ||
        StdFileTypes.CLASS.getDefaultExtension == extention && (myIndex.isInLibraryClasses(file) || SyntheticResolveUtil.syntheticResolveScope.contains(file)))
  }
}