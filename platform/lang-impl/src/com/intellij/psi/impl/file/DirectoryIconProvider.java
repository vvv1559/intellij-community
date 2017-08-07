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

package com.intellij.psi.impl.file;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IconProvider;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.roots.ui.configuration.SourceRootPresentation;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DirectoryIconProvider extends IconProvider implements DumbAware {
  @Override
  public Icon getIcon(@NotNull final PsiElement element, final int flags) {
    if (element instanceof PsiDirectory) {
      final PsiDirectory psiDirectory = (PsiDirectory)element;
      return getDirectoryIcon(psiDirectory.getVirtualFile(), psiDirectory.getProject());
    }
    return null;
  }

  public static Icon getDirectoryIcon(VirtualFile vFile, Project project) {
    SourceFolder sourceFolder = ProjectRootsUtil.getModuleSourceRoot(vFile, project);
    if (sourceFolder != null) {
      return SourceRootPresentation.getSourceRootIcon(sourceFolder);
    }
    else {
      Icon excludedIcon = getIconIfExcluded(project, vFile);
      return excludedIcon != null ? excludedIcon : PlatformIcons.DIRECTORY_CLOSED_ICON;
    }
  }

  @Nullable
  public static Icon getIconIfExcluded(@NotNull Project project, @NotNull VirtualFile vFile) {
    if (!Registry.is("ide.hide.excluded.files")) {
      boolean ignored = ProjectRootManager.getInstance(project).getFileIndex().isExcluded(vFile);
      if (ignored) {
        return AllIcons.Modules.ExcludeRoot;
      }
    }
    return null;
  }
}
