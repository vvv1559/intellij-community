/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.unscramble;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface UnscrambleSupport<T extends JComponent> {
  ExtensionPointName<UnscrambleSupport> EP_NAME = ExtensionPointName.create("com.intellij.unscrambleSupport");

  @Nullable
  default String unscramble(@NotNull Project project, @NotNull String text, @NotNull String logName, @Nullable T settings) {
    return unscramble(project, text, logName);
  }

  @NotNull
  String getPresentableName();

  @Nullable
  default T createSettingsComponent() {
    return null;
  }

  /**
   * @deprecated override {@link #unscramble(Project, String, String, JComponent)} instead
   */
  @Nullable
  default String unscramble(Project project, String text, String logName) {
    return null;
  }
}