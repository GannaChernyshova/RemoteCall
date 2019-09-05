package com.farpost.intellij.remotecall.updater;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SampleDialogWrapper extends DialogWrapper {

  public SampleDialogWrapper() {
    super(true); // use current window as parent
    init();
    setTitle("Test DialogWrapper");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    JPanel dialogPanel = new JPanel(new BorderLayout());

    JLabel label = new JLabel("testing");
    label.setPreferredSize(new Dimension(100, 100));
    dialogPanel.add(label, BorderLayout.CENTER);

    JButton testButton = new JButton();
    testButton.addActionListener(actionEvent -> {
      if(new SampleDialogWrapper().showAndGet()) {
        // user pressed ok
      }
    });

    return dialogPanel;
  }


}
