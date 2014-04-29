package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.language

import com.intellij.openapi.fileTypes.{FileTypeConsumer, FileTypeFactory}

/**
 * @author stasstels
 * @since  4/28/14.
 */
class ScamFileTypeFactory extends FileTypeFactory {
  override def createFileTypes(consumer: FileTypeConsumer): Unit = {
    consumer.consume(ScamFileType)
  }
}
