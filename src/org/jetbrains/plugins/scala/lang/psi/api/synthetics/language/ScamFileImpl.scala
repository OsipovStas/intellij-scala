package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.language

import com.intellij.psi.FileViewProvider
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaFileImpl

/**
 * @author stasstels
 * @since  4/28/14.
 */
class ScamFileImpl(provider: FileViewProvider) extends ScalaFileImpl(provider, ScamFileType) {

  override def isScriptFile: Boolean = true

  //  override def getFileResolveScope: GlobalSearchScope = {
  //    val resolveScope = super.getFileResolveScope
  //    new AdditionalIndexedRootsScope(resolveScope, new AdditionalIndexableFileSet(DSLRootProvider))
  //  }
  //
  //  override def getResolveScope: GlobalSearchScope = new AdditionalIndexedRootsScope(super.getResolveScope, new AdditionalIndexableFileSet(DSLRootProvider))
}
