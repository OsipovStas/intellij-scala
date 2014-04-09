package org.jetbrains.plugins.scala
package lang.psi.api.annotations.typedef

import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunction, ScAnnotationsHolder}
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.AnnotationHolder
import org.jetbrains.plugins.scala.lang.psi.api.annotations.base.{SyntheticAnnotations, FakeCreator}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import scala.Long
import scala.Some

/**
 * @author stasstels
 * @since  3/31/14.
 */
trait SyntheticOwner extends ScAnnotationsHolder with ScMember {

  @volatile
  private var cachedMethods: Map[FakeCreator, (ScFunction, Long)] = Map()

  @volatile
  private var cache: Seq[ScFunction] = null

  @volatile
  private var modCount: Long = 0L


  def getSyntheticMember(creator: FakeCreator): ScFunction = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    cachedMethods.get(creator) match {
      case Some(v) if v._2 == curModCount =>
        v._1
      case _ =>
        val v = (creator.createMember(this), curModCount)
        cachedMethods = cachedMethods.updated(creator, v)
        v._1
    }
  }


  def getSynthetics: Seq[ScFunction] = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if(cache != null && curModCount == modCount) cache
    else {
      val res: Seq[ScFunction] = SyntheticAnnotations.getCreatorsFor(this).map {
        case creator => getSyntheticMember(creator)
      }
      modCount = curModCount
      cache = res
      res
    }
  }


  def asAnnotationHolder: AnnotationHolder



}
