package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing._
import java.awt.{Dimension, CardLayout, Color, BorderLayout}
import com.intellij.ui.SeparatorComponent
import com.intellij.openapi.ui.ex.MultiLineLabel

/**
 * @author stasstels
 * @since  5/9/14.
 */
class ScamScriptsWizard(project: Project) extends DialogWrapper(project, true) {
  private val myIcon: JLabel = new JLabel
  private val myHeader: JLabel = new JLabel
  private val myExplanation: JLabel = new MultiLineLabel
  private var myStepContent: JPanel = null
  private var myCardLayout: CardLayout = null
  init()

  def getWindowPreferredSize: Dimension = new Dimension(600, 350)

  override def createCenterPanel() = {
    val result: JPanel = new JPanel(new BorderLayout)
    val icon: JPanel = new JPanel(new BorderLayout)
    icon.add(myIcon, BorderLayout.NORTH)
    result.add(icon, BorderLayout.WEST)
    val header: JPanel = new JPanel
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS))
    header.add(myHeader)
    header.add(Box.createVerticalStrut(4))
    header.add(myExplanation)
    header.add(Box.createVerticalStrut(4))
    header.add(new SeparatorComponent(0, Color.gray, null))
    header.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2))
    val content: JPanel = new JPanel(new BorderLayout(12, 12))
    content.add(header, BorderLayout.NORTH)
    myCardLayout = new CardLayout

    myStepContent = new JPanel(myCardLayout) {

      override def getPreferredSize: Dimension = {
        getWindowPreferredSize
      }
    }
    content.add(header, BorderLayout.NORTH)
    content.add(myStepContent, BorderLayout.CENTER)
    result.add(content, BorderLayout.CENTER)
    result
  }

}
