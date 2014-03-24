package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel

import fake.FakePsiMethod
import types.result.{TypingContext, TypingContextOwner}
import types.ScType
import types.nonvalue.Parameter
import com.intellij.psi.{PsiElement, PsiClass, PsiMethod}
import com.intellij.util.containers.ConcurrentHashMap
import light.{PsiClassWrapper, StaticPsiTypedDefinitionWrapper, PsiTypedDefinitionWrapper}
import extensions.toSeqExt
import org.jetbrains.plugins.scala.lang.psi.api.annotations.{SyntheticMemberCreator, MacroAnnotations}
import org.jetbrains.plugins.scala.lang.psi.api.annotations.beans.BeanMacroGenerator.{GetterCreator, SetterCreator}
import org.jetbrains.plugins.scala.lang.psi.api.annotations.beans.BooleanBeanMacroGenerator

/**
 * Member definitions, classes, named patterns which have types
 */
trait ScTypedDefinition extends ScNamedElement with TypingContextOwner {

  /**
   * @return false for variable elements
   */
  def isStable = true


  @volatile
  private var underEqualsMethodsCache: PsiMethod = null
  @volatile
  private var underEqualsModCount: Long = 0L





  def getUnderEqualsMethod: PsiMethod = {
    def inner(): PsiMethod = {
      val hasModifierProperty: String => Boolean = nameContext match {
        case v: ScModifierListOwner => v.hasModifierProperty
        case _ => _ => false
      }
      val tType = getType(TypingContext.empty).getOrAny
      implicit def arr2arr(a: Array[ScType]): Array[Parameter] = a.toSeq.mapWithIndex {
        case (tpe, index) => new Parameter("", None, tpe, false, false, false, index)
      }.toArray
      new FakePsiMethod(this, name + "_=", Array[ScType](tType), types.Unit, hasModifierProperty)
    }

    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (underEqualsMethodsCache != null && underEqualsModCount == curModCount) return underEqualsMethodsCache
    val res = inner()
    underEqualsModCount = curModCount
    underEqualsMethodsCache = res
    res
  }

  def getGetBeanMethod: PsiMethod = {

    getSyntheticMember(GetterCreator)
  }

  def getSetBeanMethod: PsiMethod = {

    getSyntheticMember(SetterCreator)
  }

  def getIsBeanMethod: PsiMethod = {
    getSyntheticMember(BooleanBeanMacroGenerator.IsGetterCreator)
  }

  def getSynthetics: Seq[PsiMethod] = {
    getSyntheticsStub
  }


  import PsiTypedDefinitionWrapper.DefinitionRole._
  private val typedDefinitionWrapper: ConcurrentHashMap[(Boolean, Boolean, DefinitionRole, Option[PsiClass]), (PsiTypedDefinitionWrapper, Long)] =
    new ConcurrentHashMap()

  def getTypedDefinitionWrapper(isStatic: Boolean, isInterface: Boolean, role: DefinitionRole,
                                cClass: Option[PsiClass] = None): PsiTypedDefinitionWrapper = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    val r = typedDefinitionWrapper.get(isStatic, isInterface, role, cClass)
    if (r != null && r._2 == curModCount) {
      return r._1
    }
    val res = new PsiTypedDefinitionWrapper(this, isStatic, isInterface, role, cClass)
    typedDefinitionWrapper.put((isStatic, isInterface, role, cClass), (res, curModCount))
    res
  }

  private val staticTypedDefinitionWrapper: ConcurrentHashMap[(DefinitionRole, PsiClassWrapper), (StaticPsiTypedDefinitionWrapper, Long)] =
    new ConcurrentHashMap()

  def getStaticTypedDefinitionWrapper(role: DefinitionRole, cClass: PsiClassWrapper): StaticPsiTypedDefinitionWrapper = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    val r = staticTypedDefinitionWrapper.get(role, cClass)
    if (r != null && r._2 == curModCount) {
      return r._1
    }
    val res = new StaticPsiTypedDefinitionWrapper(this, role, cClass)
    staticTypedDefinitionWrapper.put((role, cClass), (res, curModCount))
    res
  }

  def nameContext: PsiElement = ScalaPsiUtil.nameContext(this)
  def isVar: Boolean = false
  def isVal: Boolean = false

  @volatile
  private var cachedMap: Map[SyntheticMemberCreator, (PsiMethod, Long)] = Map()

  @volatile
  private var methodCache: Seq[PsiMethod] = null

  @volatile
  private var theModCount: Long = 0L

  def getSyntheticsStub: Seq[PsiMethod] = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if(methodCache != null && curModCount == theModCount) methodCache
    else {
      val res: Seq[PsiMethod] = MacroAnnotations.getSyntheticCreatorsFor(this).map {
        case creator => getSyntheticMember(creator)
      }
      theModCount = curModCount
      methodCache = res
      res
    }
  }

  def getSyntheticMember(creator: SyntheticMemberCreator): PsiMethod = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    cachedMap.get(creator) match {
      case Some(v) if v._2 == curModCount =>
        v._1
      case _ =>
        val v = (creator.createMember(this), curModCount)
        cachedMap = cachedMap.updated(creator, v)
        v._1
    }
  }


}