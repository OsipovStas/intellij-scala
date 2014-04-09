package org.jetbrains.plugins.scala.findUsages.factory

import com.intellij.find.findUsages.{AbstractFindUsagesDialog, FindUsagesOptions, FindUsagesHandler}
import com.intellij.psi.{PsiClass, PsiNamedElement, PsiElement}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScTypedDefinition
import org.jetbrains.plugins.scala.extensions.toPsiNamedElementExt
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScTypeDefinition, ScClass, ScTrait, ScObject}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScTypeAlias, ScFunction, ScVariable, ScValue}
import org.jetbrains.plugins.scala.lang.psi.light._
import com.intellij.openapi.actionSystem.DataContext
import java.util
import com.intellij.util.Processor
import com.intellij.usageView.UsageInfo
import com.intellij.psi.search.searches.ClassInheritorsSearch
import collection.mutable
import org.jetbrains.plugins.scala.util.ScalaUtil
import scala.Array
import org.jetbrains.plugins.scala.lang.psi.impl.search.ScalaOverridingMemberSearcher
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPrimaryConstructor
import org.jetbrains.plugins.scala.lang.psi.api.annotations.typedef.SyntheticOwner
import org.jetbrains.plugins.scala.lang.psi.api.annotations.base.SyntheticAnnotations

/**
 * User: Alexander Podkhalyuzin
 * Date: 17.08.2009
 */

class ScalaFindUsagesHandler(element: PsiElement) extends FindUsagesHandler(element) {
  override def getPrimaryElements: Array[PsiElement] = Array(element)
  override def getStringsToSearch(element: PsiElement): util.Collection[String] = {
    val result: util.Set[String] = new util.HashSet[String]()
    element match {
      case t: ScTrait =>
        result.add(t.name)
        result.add(t.getName)
        result.add(t.fakeCompanionClass.getName)
      case o: ScObject =>
        result.add(o.name)
        result.add(o.getName)
      case c: ScClass if c.isCase =>
        result.add(c.name)
        c.fakeCompanionModule match {
          case Some(o) => result.add(o.getName)
          case _ =>
        }
      case named: PsiNamedElement =>
        val name = named.name
        result.add(name)
        ScalaPsiUtil.nameContext(named) match {
          case owner: SyntheticOwner =>
            SyntheticAnnotations.getCreatorsFor(owner).foreach {
              case creator => result.add(creator.transformedName(name))
            }
          case _ =>
        }
      case _ => result.add(element.getText)
    }
    result
  }

  override def getFindUsagesOptions(dataContext: DataContext): FindUsagesOptions = {
    element match {
      case t: ScTypeDefinition => new ScalaTypeDefinitionFindUsagesOptions(t, getProject, dataContext)
      case _ => super.getFindUsagesOptions(dataContext)
    }
  }

  override def getSecondaryElements: Array[PsiElement] = {
    element match {
      case t: ScObject =>
        t.fakeCompanionClass match {
          case Some(clazz) => Array(clazz)
          case _ => Array.empty
        }
      case t: ScTrait => Array(t.fakeCompanionClass)
      case t: ScTypedDefinition =>
        t.nameContext match {
          case owner: SyntheticOwner =>
            owner.getSynthetics.toArray
          case _ => Array.empty
        }
      case _ => Array.empty
    }
  }

  override def getFindUsagesDialog(isSingleFile: Boolean, toShowInNewTab: Boolean, mustOpenInNewTab: Boolean): AbstractFindUsagesDialog = {
    element match {
      case t: ScTypeDefinition => new ScalaTypeDefinitionUsagesDialog(t, getProject, getFindUsagesOptions,
        toShowInNewTab, mustOpenInNewTab, isSingleFile, this)
      case _ => super.getFindUsagesDialog(isSingleFile, toShowInNewTab, mustOpenInNewTab)
    }
  }

  override def processElementUsages(element: PsiElement, processor: Processor[UsageInfo], options: FindUsagesOptions): Boolean = {
    if (!super.processElementUsages(element, processor, options)) return false
    options match {
      case s: ScalaTypeDefinitionFindUsagesOptions if element.isInstanceOf[ScTypeDefinition] =>
        val clazz = element.asInstanceOf[ScTypeDefinition]
        if (s.isMembersUsages) {
          clazz.members.foreach {
            case fun: ScFunction =>
              if (!super.processElementUsages(fun, processor, options)) return false
            case v: ScValue =>
              v.declaredElements.foreach { d =>
                if (!super.processElementUsages(d, processor, options)) return false
              }
            case v: ScVariable =>
              v.declaredElements.foreach { d =>
                if (!super.processElementUsages(d, processor, options)) return false
              }
            case ta: ScTypeAlias =>
              if (!super.processElementUsages(ta, processor, options)) return false
            case c: ScTypeDefinition =>
              if (!super.processElementUsages(c, processor, options)) return false
            case c: ScPrimaryConstructor =>
              if (!super.processElementUsages(c, processor, options)) return false
          }
          clazz match {
            case c: ScClass =>
              c.constructor match {
                case Some(constr) => constr.effectiveParameterClauses.foreach {clause =>
                  clause.parameters.foreach {param =>
                    if (!super.processElementUsages(c, processor, options)) return false
                  }
                }
                case _ =>
              }
            case _ =>
          }
        }
        if (s.isSearchCompanionModule) {
          ScalaPsiUtil.getBaseCompanionModule(clazz) match {
            case Some(companion) =>
              if (!super.processElementUsages(companion, processor, options)) return false
            case _ =>
          }
        }
        if (s.isImplementingTypeDefinitions) {
          val res = new mutable.HashSet[PsiClass]()
          ClassInheritorsSearch.search(clazz, true).forEach(new Processor[PsiClass] {
            def process(t: PsiClass): Boolean = {
              t match {
                case p: PsiClassWrapper =>
                case _ => res += t
              }
              true
            }
          })
          res.foreach { c =>
            ScalaUtil.readAction(getProject) {
              if (!processor.process(new UsageInfo(c))) return false
            }
          }
        }
      case _ =>
    }

    element match {
      case function: ScFunction if !function.isLocal =>
        ScalaUtil.readAction(getProject) {
          for (elem <- ScalaOverridingMemberSearcher.search(function, deep = true)) {
            if (!super.processElementUsages(elem, processor, options)) return false
          }
        }
      case _ =>
    }
    true
  }
}