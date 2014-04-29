package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.language

import org.jetbrains.plugins.scala.lang.parser.ScalaFileFactory
import com.intellij.psi.FileViewProvider
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile

/**
 * @author stasstels
 * @since  4/27/14.
 */
class ScamFileFactory extends ScalaFileFactory{
  override def createFile(provider: FileViewProvider): Option[ScalaFile] = {
    Option(provider.getVirtualFile.getFileType) collect {
      case ScamFileType => new ScamFileImpl(provider)
    }
  }
}
