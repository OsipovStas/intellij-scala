package org.jetbrains.plugins.scala.lang.psi.api.synthetics.ui;

import javax.swing.*;

/**
 * @author stasstels
 * @since 5/9/14.
 */
public class ScamListForm {

//  public static class ScamScriptDescriptor {
//    public final String name;
//    public final boolean isPluged;
//
//    public ScamScriptDescriptor(String name, boolean isPluged) {
//      this.name = name;
//      this.isPluged = isPluged;
//    }
//  }

  protected JPanel myRootPanel;
  protected JList<ScamScriptDescriptor> myScriptList;
  protected JButton myEnableAllButton;
  protected JButton myDisableAllButton;
}


