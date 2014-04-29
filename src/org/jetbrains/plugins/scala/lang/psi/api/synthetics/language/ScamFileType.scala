package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.language

import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.DSL

/**
 * @author stasstels
 * @since  4/27/14.
 */
object ScamFileType extends LanguageFileType(ScalaFileType.SCALA_LANGUAGE) {

  def getName = DSL.Name

  def getDescription = DSL.FileDescription

  def getDefaultExtension = DSL.FileExtension

  def getIcon = DSL.FileIcon

}

