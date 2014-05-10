package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing._
import java.awt._
import com.intellij.ui.{CollectionListModel, ClickListener, SeparatorComponent}
import com.intellij.openapi.ui.ex.MultiLineLabel
import com.intellij.util.ui.UIUtil
import java.awt.event._
import com.intellij.openapi.util.text.StringUtil
import scala.collection.mutable
import scala.util.Try
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.SyntheticUtil

/**
 * @author stasstels
 * @since  5/9/14.
 */
class ScamScriptsWizard(project: Project) extends DialogWrapper(project, true) {


  def fromBundle(key: String) = ScalaBundle.message(key)

  val myTitleKey = "scam.wizard.title"
  val explanationKey = "scam.wizard.explanation"
  val contentKey = "scam.wizard.content.name"
  val contentTitleKey = "scam.wizard.content.title"

  val myTitle = fromBundle(myTitleKey)

  val explanation = fromBundle(explanationKey)

  val contentName = fromBundle(contentKey)

  val contentTitle = fromBundle(contentTitleKey)

  private val myIcon: JLabel = new JLabel
  private val myHeader: JLabel = new JLabel
  private val myExplanation: JLabel = new MultiLineLabel
  private val model = new ScamListModel
  private val scriptList = new ScamScriptList(model)
  private var myContent = new JPanel()
  private var myCardLayout: CardLayout = null
  init()

  def getWindowPreferredSize: Dimension = new Dimension(600, 350)

  def getContent: JComponent = scriptList.prepare

  override def init() {
    setTitle(myTitle)
    super.init()
    initContent()
  }

  def initContent() {
    scriptList.fillScripts()
    myHeader.setFont(myHeader.getFont.deriveFont(Font.BOLD, 14))
    myHeader.setText(contentTitle)
    myExplanation.setText(explanation)
    myContent.add(getContent, contentName)
    myCardLayout.show(myContent, contentName)
  }

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

    myContent = new JPanel(myCardLayout) {

      override def getPreferredSize: Dimension = {
        getWindowPreferredSize
      }
    }
    content.add(header, BorderLayout.NORTH)
    content.add(myContent, BorderLayout.CENTER)
    result.add(content, BorderLayout.CENTER)
    result
  }

  def enabledScripts = model.allScripts.filterNot(s => model.disabledScripts.contains(s.name))
  
  def disabledScripts = model.allScripts.filter(s => model.disabledScripts.contains(s.name))

  private class ScamListModel {
    val allScripts = findScamScripts
    val disabledScripts: mutable.HashSet[String] = mutable.HashSet(allScripts.filterNot(_.isPluged).map(_.name): _*)

    def setScriptEnable(s: ScamScriptDescriptor, value: Boolean) {
      if (value) {
        disabledScripts -= s.name
      } else {
        disabledScripts += s.name
      }
    }

    def findScamScripts: Seq[ScamScriptDescriptor] = {
      SyntheticUtil.findScamScripts(project).map {
        case file => ScamScriptDescriptor(file, SyntheticUtil.isPluged(file, project))
      }
    }


    def isDisabledScript(script: ScamScriptDescriptor) = disabledScripts.contains(script.name)

  }

  private class ScamScriptList(model: ScamListModel) extends ScamListForm {
    val panel = myRootPanel
    val scriptList = myScriptList
    val enable = myEnableAllButton
    val disable = myDisableAllButton

    {
      scriptList.setCellRenderer(new CellRenderer)
      val clickableArea: Int = new JCheckBox("").getMinimumSize.width
      new ClickListener {
        def onClick(e: MouseEvent, clickCount: Int): Boolean = {
          if (e.getX < clickableArea) {
            toggleSelection()
          }
          true
        }
      }.installOn(scriptList)

      scriptList.addKeyListener(new KeyAdapter {
        override def keyTyped(e: KeyEvent) {
          if (e.getKeyChar == ' ') {
            toggleSelection()
          }
        }
      })

      enable.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          setAllPluginsEnabled(value = true)
        }
      })

      disable.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          setAllPluginsEnabled(value = false)
        }
      })
    }

    def toggleSelection() = {
      getSelectedScript.foreach {
        case s =>
          val willEnable = model.isDisabledScript(s)
          model.setScriptEnable(s, willEnable)
      }
      scriptList.repaint()
    }

    def getSelectedScript = {
      val index = scriptList.getSelectionModel.getLeadSelectionIndex
      Try(model.allScripts(index)).toOption
    }


    def setAllPluginsEnabled(value: Boolean) {
      model.allScripts.foreach(model.setScriptEnable(_, value))
      scriptList.repaint()
    }

    class CellRenderer extends ListCellRenderer[ScamScriptDescriptor] {

      override def getListCellRendererComponent(list: JList[_ <: ScamScriptDescriptor], value: ScamScriptDescriptor, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
        val checkbox = new JCheckBox()
        checkbox.setEnabled(true)
        if (isSelected) {
          checkbox.setBackground(UIUtil.getListSelectionBackground)
          checkbox.setForeground(UIUtil.getListSelectionForeground)
        } else {
          checkbox.setBackground(UIUtil.getListBackground)
          checkbox.setForeground(UIUtil.getListForeground)
        }
        checkbox.setText(value.name)
        if (value.isPluged) {
          checkbox.setFont(checkbox.getFont.deriveFont(Font.BOLD))
          checkbox.setForeground(new Color(0, 160, 0))
        }
        checkbox.setSelected(!model.isDisabledScript(value))
        checkbox
      }

    }

    def fillScripts() {
      import scala.collection.JavaConversions._
      val sorted = model.allScripts.sortWith {
        case (s1, s2) => StringUtil.compare(s1.name, s2.name, true) > 0
      }
      val listModel: ListModel[ScamScriptDescriptor] = new CollectionListModel[ScamScriptDescriptor](sorted).asInstanceOf[ListModel[ScamScriptDescriptor]]
      scriptList.setModel(listModel)
      scriptList.setSelectedIndex(0)
    }


    def prepare: JComponent = {
      panel.revalidate()
      panel
    }

  }

}

case class ScamScriptDescriptor(script: VirtualFile, isPluged: Boolean) {
  def name: String = script.getName
}