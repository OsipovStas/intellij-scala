package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.resolve

import com.intellij.psi.NonClasspathClassFinder
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.caches.ScalaShortNamesCacheManager
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass

/**
 * @author stasstels
 * @since  5/8/14.
 */
class ScamClassFinder(project: Project) extends NonClasspathClassFinder(project) {

  override protected def calcClassRoots() = {
    import scala.collection.JavaConversions._
    SyntheticResolveUtil.syntheticRootList
  }
}
