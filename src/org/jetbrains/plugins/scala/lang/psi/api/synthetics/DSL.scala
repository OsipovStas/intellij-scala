package org.jetbrains.plugins.scala
package lang.psi.api.synthetics

import javax.swing.Icon
import com.intellij.openapi.util.IconLoader
import com.intellij.util.PathUtil
import com.intellij.openapi.vfs.VfsUtil
import java.nio.file.Paths
import org.jetbrains.plugins.scala.dsl.tree.Empty
import com.intellij.openapi.util.io.FileUtil

/**
 * @author stasstels
 * @since  4/27/14.
 */
object DSL {

  lazy val FileIcon: Icon = IconLoader.getIcon("/icons/scala_scam_icon_main.png")

  lazy val Icon: Icon = IconLoader.getIcon("/icons/scala_scam_icon_main.png")

  lazy val fakeMethodIcon = IconLoader.getIcon("/icons/fake.png")

  val FileExtension: String = "scam"

  val FileDescription: String = "Synthetics DSL file"

  val Name: String = "Scam"

  lazy val dslJarUrl = VfsUtil.getUrlForLibraryRoot(Paths.get(PathUtil.getJarPathForClass(Empty.getClass)).toFile)

  val SCAM_MARKER = "_$SCAM$_"

  lazy val scamScriptDir = {
    val dir = Paths.get(Paths.get(PathUtil.getJarPathForClass(Empty.getClass)).getParent.toString, "scams").toFile
    FileUtil.createDirectory(dir)
    dir
  }
}
