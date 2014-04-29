package org.jetbrains.plugins.scala
package lang.psi.api.synthetics

import javax.swing.Icon
import com.intellij.openapi.util.IconLoader
import com.intellij.util.PathUtil
import org.jetbrains.plugins.scala.dsl.Member
import com.intellij.openapi.vfs.{VfsUtilCore, VirtualFileManager, VirtualFile, VfsUtil}
import java.io.File
import com.intellij.psi.search.NonClasspathDirectoryScope
import java.nio.file.{Paths, Files}
import java.net.URL
import com.intellij.openapi.vfs.newvfs.ManagingFS
import com.intellij.util.indexing.AdditionalIndexedRootsScope

/**
 * @author stasstels
 * @since  4/27/14.
 */
object DSL {

  lazy val FileIcon: Icon = IconLoader.getIcon("/icons/scala_scam_icon_main.png")

  lazy val Icon: Icon = IconLoader.getIcon("/icons/scala_scam_icon_main.png")

  val FileExtension: String = "scam"

  val FileDescription: String = "Synthetics DSL file"

  val Name: String = "Scam"

  lazy val dslJarUrl = VfsUtil.getUrlForLibraryRoot(Paths.get(PathUtil.getJarPathForClass(Member.getClass)).toFile)


}
