/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.actionSystem.impl;

import com.intellij.openapi.actionSystem.ex.ActionButtonLook;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

import static com.intellij.openapi.actionSystem.ActionButtonComponent.*;

public class Win10ActionButtonLook extends ActionButtonLook {
  @Override
  public void paintBackground(Graphics g, JComponent component, int state) {
    if (state != NORMAL) {
      g.setColor(getBackgroundColorForState(state));
      g.fillRect(0, 0, component.getWidth(), component.getHeight());
    }
  }

  private static Color getBackgroundColorForState(int state) {
    switch (state) {
      case POPPED:
        return UIManager.getColor("Button.intellij.native.focusedBackgroundColor");
      case PUSHED:
      case SELECTED:
        return UIManager.getColor("Button.intellij.native.pressedBackgroundColor");
      default:
        return UIManager.getColor("Button.background");
    }
  }

  @Override
  public void paintBorder(Graphics g, JComponent component, int state) {
    if (state != NORMAL) {
      Graphics2D g2 = (Graphics2D)g.create();
      try {
        g2.setColor(getBorderColorForState(state));

        Rectangle outerRect = new Rectangle(component.getSize());
        Path2D border = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        border.append(outerRect, false);

        Rectangle innerRect = new Rectangle(outerRect);
        JBInsets.removeFrom(innerRect, JBUI.insets(1));
        border.append(innerRect, false);

        g2.fill(border);
      } finally {
        g2.dispose();
      }
    }
  }

  private static Color getBorderColorForState(int state) {
    switch (state) {
      case POPPED:
        return UIManager.getColor("Button.intellij.native.focusedBorderColor");
      case PUSHED:
      case SELECTED:
        return UIManager.getColor("Button.intellij.native.pressedBorderColor");
      default:
        return UIManager.getColor("Button.intellij.native.borderColor");
    }
  }
}
