package org.jetbrains.plugins.scala.lang.psi.implicits

import api.expr.ScExpression
import api.statements.ScFunctionDefinition
import api.toplevel.imports.usages.ImportUsed
import caches.CashesUtil
import collection.mutable.{HashMap, HashSet}
import com.intellij.openapi.util.Key
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.util.{CachedValue, PsiModificationTracker}
import com.intellij.psi.{ResolveResult, PsiNamedElement, ResolveState, PsiElement}
import resolve.{ScalaResolveResult, ResolveTargets, BaseProcessor}

import types._

/**
 * @author ilyas
 *
 * Mix-in implementing functionality to collect [and possibly apply] implicit conversions
 */

trait ScImplicitlyConvertible extends ScalaPsiElement {
  self: ScExpression =>

  /**
   * Get all implicit types for given expression
   */
  def getImplicitTypes : List[ScType] = {
      implicitMap.keySet.toList
  }

  /**
   * Get all imports used to obtain implicit conversions for given type
   */
  def getImportsForImplicit(t: ScType): Set[ImportUsed] = implicitMap.get(t).map(s => s.flatMap(p => p._2.toList)) match {
    case Some(s) => s
    case None => Set()
  }

  private def implicitMap = CashesUtil.get(
      this, ScImplicitlyConvertible.IMPLICIT_CONVERIONS_KEY,
      new CashesUtil.MyProvider(this, {ic: ScImplicitlyConvertible => ic.buildImplicitMap})
        (PsiModificationTracker.MODIFICATION_COUNT)
    )


  private def buildImplicitMap : collection.Map[ScType, Set[(ScFunctionDefinition, Set[ImportUsed])]] = {
    val processor = new CollectImplicitsProcessor(getType)

    // Collect implicit conversions from botom to up
    def treeWalkUp(place: PsiElement, lastParent: PsiElement) {
      place match {
        case null =>
        case p => {
          if (!p.processDeclarations(processor,
            ResolveState.initial,
            lastParent, this)) return
          if (!processor.changedLevel) return
          treeWalkUp(place.getContext, place)
        }
      }
    }
    treeWalkUp(this, null)

    val sigsFound = processor.signatures.filter((sig: Signature) => {
      val types = sig.types
      types.length == 1 && getType.conforms(types(0))
    })

    val result = new HashMap[ScType, Set[(ScFunctionDefinition, Set[ImportUsed])]]

    for (signature <- sigsFound) {
      val set = processor.sig2Method(signature)
      for ((imports, fun) <- set) {
        val rt = signature.substitutor.subst(fun.returnType)
        if (!result.contains(rt)) {
          result += (rt -> Set((fun, imports)))
        } else {
          result += (rt -> (result(rt) + (Pair(fun, imports))))
        }
      }
    }
    //todo cache value!
    result
  }


  import ResolveTargets._
  class CollectImplicitsProcessor(val eType: ScType) extends BaseProcessor(Set(METHOD)) {
    private val signatures2ImplicitMethods = new HashMap[Signature, Set[Pair[Set[ImportUsed], ScFunctionDefinition]]]

    def signatures = signatures2ImplicitMethods.keySet.toArray[Signature]

    def sig2Method = signatures2ImplicitMethods

    def execute(element: PsiElement, state: ResolveState) = {

      val implicitSubstitutor = new ScSubstitutor {
        override def subst(t: ScType): ScType = t match {
          case tpt: ScTypeParameterType => eType
          case _ => super.subst(t)
        }

        override def followed(s: ScSubstitutor): ScSubstitutor = s
      }

      element match {
        case named: PsiNamedElement if kindMatches(element) => named match {
          case f: ScFunctionDefinition
            // Collect implicit conversions only
            if f.hasModifierProperty("implicit") &&
                    f.getParameterList.getParametersCount == 1 => {
            val sign = new PhysicalSignature(f, implicitSubstitutor)
            if (!signatures2ImplicitMethods.contains(sign)) {
              val newFSet = Set((getImports(state), f))
              signatures2ImplicitMethods += (sign -> newFSet)
            } else {
              signatures2ImplicitMethods += (sign -> (signatures2ImplicitMethods(sign) + Pair(getImports(state), f)))
            }
            candidatesSet += new ScalaResolveResult(f, getSubst(state), getImports(state))
          }
          //todo add implicit objects
          case _ =>
        }
        case _ =>
      }
      true

    }
  }

  protected object MyImplicitCollector {
  }

}

object ScImplicitlyConvertible {
  val IMPLICIT_CONVERIONS_KEY: Key[CachedValue[collection.Map[ScType, Set[(ScFunctionDefinition, Set[ImportUsed])]]]] = Key.create("implicit.conversions.key")
}
