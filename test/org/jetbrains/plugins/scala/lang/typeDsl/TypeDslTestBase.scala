package org.jetbrains.plugins.scala
package lang
package typeDsl

import com.intellij.openapi.vfs.{CharsetToolkit, LocalFileSystem}
import java.io.File
import java.lang.String
import org.jetbrains.plugins.scala.lang.psi.api.{ScalaRecursiveElementVisitor, ScalaFile}
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.util.io.FileUtil
import base.ScalaLightPlatformCodeInsightTestCaseAdapter
import org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil
import junit.framework.Assert._
import org.jetbrains.plugins.scala.lang.psi.types.result.Success
import org.jetbrains.plugins.scala.lang.psi.types.result.Failure

/**
 * User: Alexander Podkhalyuzin
 * Date: 10.03.2009
 */

abstract class TypeDslTestBase extends ScalaLightPlatformCodeInsightTestCaseAdapter {
  def folderPath: String = baseRootPath() + "typeDsl/"

  def testConversion(pat: ScValue) {
    assert(pat != null, "Not specified expression in range to check conformance.")
    pat.getType(TypingContext.empty) match {
      case Success(tp, _) =>
        val scalaType = tp.asScalaType
        val scType = SyntheticUtil.ScalaType2ScType(scalaType, pat.getContext, pat)
        if(!scType.equiv(tp))
          fail("Dsl Type conversion wrong " + tp.presentableText)
      case Failure(msg, elem) => assert(assertion = false, message = msg + " :: " + elem.get.getText)
    }
  }


  protected def doTest() {
    val filePath = folderPath + getTestName(false) + ".scala"
    val file = LocalFileSystem.getInstance.findFileByPath(filePath.replace(File.separatorChar, '/'))
    assert(file != null, "file " + filePath + " not found")
    val fileText = StringUtil.convertLineSeparators(FileUtil.loadFile(new File(file.getCanonicalPath), CharsetToolkit.UTF8))
    configureFromFileTextAdapter(getTestName(false) + ".scala", fileText)
    val scalaFile = getFileAdapter.asInstanceOf[ScalaFile]
    scalaFile.accept(new ScalaRecursiveElementVisitor {

      override def visitValue(v: ScValue): Unit = {
        testConversion(v)
        super.visitValue(v)
      }
    })
  }
}

