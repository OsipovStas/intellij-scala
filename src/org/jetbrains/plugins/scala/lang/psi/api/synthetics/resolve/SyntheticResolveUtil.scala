package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.resolve

import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.DSL
import com.intellij.psi.search.{NonClasspathDirectoryScope, GlobalSearchScope}

/**
 * @author stasstels
 * @since  5/7/14.
 */
object SyntheticResolveUtil {

  val getSytheticJarVirtualFile = VirtualFileManager.getInstance().findFileByUrl(DSL.dslJarUrl)

  val syntheticResolveScope = Option(getSytheticJarVirtualFile).fold(GlobalSearchScope.EMPTY_SCOPE)(new NonClasspathDirectoryScope(_))

  def patchedResolveScope(scope: GlobalSearchScope) = scope.uniteWith(syntheticResolveScope)

  val syntheticRootList = Option(getSytheticJarVirtualFile).toList

}
