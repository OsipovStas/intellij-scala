package org.jetbrains.plugins.scala
package lang.psi.api.annotations

/**
 * @author stasstels
 * @since  4/4/14.
 */
object SyntheticUtils {


  def getParameterString(types: Seq[String]): String = {
    import extensions.toSeqExt
    types.mapWithIndex {
      case (typeText, index) => s"arg$index: $typeText"
    } mkString("(", ",", ")")
  }


}
