package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.resolve

import com.intellij.util.indexing.IndexableSetContributor
import java.util
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author stasstels
 * @since  5/8/14.
 */
class ScamIndexableSetContributor extends IndexableSetContributor {
  override def getAdditionalRootsToIndex: util.Set[VirtualFile] = {
    import scala.collection.JavaConversions._
    Option(SyntheticResolveUtil.getSytheticJarVirtualFile).toSet[VirtualFile]
  }

}
