package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel

import fake.FakePsiMethod
import types.result.{TypingContext, TypingContextOwner}
import com.intellij.openapi.util.text.StringUtil
import types.ScType
import statements.params.ScClassParameter
import statements.{ScAnnotationsHolder, ScVariable, ScValue}
import types.nonvalue.Parameter
import com.intellij.psi.{PsiElement, PsiClass, PsiMethod}
import com.intellij.util.containers.ConcurrentHashMap
import light.{PsiClassWrapper, StaticPsiTypedDefinitionWrapper, PsiTypedDefinitionWrapper}
import extensions.toSeqExt
import org.jetbrains.plugins.scala.lang.psi.api.annotations.MacroAnnotations

/**
 * Member definitions, classes, named patterns which have types
 */
trait ScTypedDefinition extends ScNamedElement with TypingContextOwner {

  /**
   * @return false for variable elements
   */
  def isStable = true
  
  @volatile
  private var beanMethodsCache: Seq[PsiMethod] = null
  @volatile
  private var modCount: Long = 0L

  @volatile
  private var isBeanMethodsCache: PsiMethod = null
  @volatile
  private var isModCount: Long = 0L

  @volatile
  private var getBeanMethodsCache: PsiMethod = null
  @volatile
  private var getModCount: Long = 0L

  @volatile
  private var setBeanMethodsCache: PsiMethod = null
  @volatile
  private var setModCount: Long = 0L

  @volatile
  private var underEqualsMethodsCache: PsiMethod = null
  @volatile
  private var underEqualsModCount: Long = 0L

  @volatile
  private var fakeMethodsCache: Seq[PsiMethod] = null
  @volatile
  private var fakeModCount: Long = 0L




  def getUnderEqualsMethod: PsiMethod = {
    def inner(): PsiMethod = {
      val hasModifierProperty: String => Boolean = nameContext match {
        case v: ScModifierListOwner => v.hasModifierProperty _
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
    def inner(): PsiMethod = {
      val hasModifierProperty: String => Boolean = nameContext match {
        case v: ScModifierListOwner => v.hasModifierProperty _
        case _ => _ => false
      }
      val tType: ScType = this.getType(TypingContext.empty).getOrAny
      new FakePsiMethod(this, "get" + StringUtil.capitalize(this.name), Array.empty,
        tType, hasModifierProperty)
    }

    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (getBeanMethodsCache != null && getModCount == curModCount) return getBeanMethodsCache
    val res = inner()
    getModCount = curModCount
    getBeanMethodsCache = res
    res
  }

  def getSetBeanMethod: PsiMethod = {
    def inner(): PsiMethod = {
      val hasModifierProperty: String => Boolean = nameContext match {
        case v: ScModifierListOwner => v.hasModifierProperty _
        case _ => _ => false
      }
      val tType = getType(TypingContext.empty).getOrAny
      implicit def arr2arr(a: Array[ScType]): Array[Parameter] = a.toSeq.mapWithIndex {
        case (tpe, index) => new Parameter("", None, tpe, false, false, false, index)
      }.toArray
      new FakePsiMethod(this, "set" + name.capitalize, Array[ScType](tType), types.Unit, hasModifierProperty)
    }

    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (setBeanMethodsCache != null && setModCount == curModCount) return setBeanMethodsCache
    val res = inner()
    setModCount = curModCount
    setBeanMethodsCache = res
    res
  }

  def getIsBeanMethod: PsiMethod = {
    def inner(): PsiMethod = {
      val hasModifierProperty: String => Boolean = nameContext match {
        case v: ScModifierListOwner => v.hasModifierProperty _
        case _ => _ => false
      }
      new FakePsiMethod(this, "is" + StringUtil.capitalize(this.name), Array.empty,
        this.getType(TypingContext.empty).getOrAny, hasModifierProperty)
    }

    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (isBeanMethodsCache != null && isModCount == curModCount) return isBeanMethodsCache
    val res = inner()
    isModCount = curModCount
    isBeanMethodsCache = res
    res
  }

  def getBeanMethods: Seq[PsiMethod] = {
    def getBeanMethodsInner(t: ScTypedDefinition): Seq[PsiMethod] = {
      def valueSeq(v: ScAnnotationsHolder with ScModifierListOwner): Seq[PsiMethod] = {
        val beanProperty = ScalaPsiUtil.isBeanProperty(v)
        val booleanBeanProperty = ScalaPsiUtil.isBooleanBeanProperty(v)
        if (beanProperty || booleanBeanProperty) {
          Seq(if (beanProperty) getGetBeanMethod else getIsBeanMethod)
        } else Seq.empty
      }
      def variableSeq(v: ScAnnotationsHolder with ScModifierListOwner): Seq[PsiMethod] = {
        val beanProperty = ScalaPsiUtil.isBeanProperty(v)
        val booleanBeanProperty = ScalaPsiUtil.isBooleanBeanProperty(v)
        if (beanProperty || booleanBeanProperty) {
          Seq(if (beanProperty) getGetBeanMethod else getIsBeanMethod, getSetBeanMethod)
        } else Seq.empty
      }
      ScalaPsiUtil.nameContext(this) match {
        case v: ScValue =>
          valueSeq(v)
        case v: ScVariable =>
          variableSeq(v)
        case v: ScClassParameter if v.isVal =>
          valueSeq(v)
        case v: ScClassParameter if v.isVar =>
          variableSeq(v)
        case _ => Seq.empty
      }
    }
    
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (beanMethodsCache != null && modCount == curModCount) return beanMethodsCache
    val res = getBeanMethodsInner(this) ++ getFakeMethods
    modCount = curModCount
    beanMethodsCache = res
    res
  }

  def getFakeMethods: Seq[PsiMethod] = {
    val curModCount = getManager.getModificationTracker.getOutOfCodeBlockModificationCount
    if (fakeMethodsCache != null && fakeModCount == curModCount) return fakeMethodsCache
    val res = MacroAnnotations.getFakeMethods(this)
    fakeModCount = curModCount
    fakeMethodsCache = res
    res
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
}