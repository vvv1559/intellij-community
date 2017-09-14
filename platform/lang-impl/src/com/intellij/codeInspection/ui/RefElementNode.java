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

package com.intellij.codeInspection.ui;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.CommonProblemDescriptor;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.reference.RefDirectory;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.lang.annotation.HighlightSeverity;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author max
 */
public class RefElementNode extends SuppressableInspectionTreeNode {
  private volatile boolean myHasDescriptorsUnder;
  private volatile CommonProblemDescriptor mySingleDescriptor;
  private final Icon myIcon;
  public RefElementNode(@Nullable RefEntity userObject, @NotNull InspectionToolPresentation presentation) {
    super(userObject, presentation);
    init(presentation.getContext().getProject());
    final RefEntity refEntity = getElement();
    myIcon = refEntity == null ? null : refEntity.getIcon(false);
  }

  @Override
  public final boolean isAlreadySuppressedFromView() {
    return getElement() != null && getPresentation().isSuppressed(getElement());
  }

  public boolean hasDescriptorsUnder() {
    return myHasDescriptorsUnder;
  }

  @Nullable
  public RefEntity getElement() {
    return (RefEntity)getUserObject();
  }

  @Override
  @Nullable
  public Icon getIcon(boolean expanded) {
    return myIcon;
  }

  @Override
  protected String calculatePresentableName() {
    final RefEntity element = getElement();
    if (element == null) {
      return InspectionsBundle.message("inspection.reference.invalid");
    }
    return element.getRefManager().getRefinedElement(element).getName();
  }

  @Override
  protected boolean calculateIsValid() {
    final RefEntity refEntity = getElement();
    return refEntity != null && refEntity.isValid();
  }

  @Override
  public boolean isExcluded() {
    RefEntity element = getElement();
    if (isLeaf() && element != null) {
      return getPresentation().isExcluded(element);
    }
    return super.isExcluded();
  }

  @Override
  public void excludeElement() {
    RefEntity element = getElement();
    if (isLeaf() && element != null) {
      getPresentation().exclude(element);
      return;
    }
    super.excludeElement();
  }

  @Override
  public void amnestyElement() {
    RefEntity element = getElement();
    if (isLeaf() && element != null) {
      getPresentation().amnesty(element);
      return;
    }
    super.amnestyElement();
  }

  @Override
  public void add(MutableTreeNode newChild) {
    checkHasDescriptorUnder(newChild);
    super.add(newChild);
  }

  @Override
  public InspectionTreeNode insertByOrder(InspectionTreeNode child, boolean allowDuplication) {
    checkHasDescriptorUnder(child);
    return super.insertByOrder(child, allowDuplication);
  }

  public void setProblem(CommonProblemDescriptor descriptor) {
    mySingleDescriptor = descriptor;
  }

  @Nullable
  @Override
  public CommonProblemDescriptor getDescriptor() {
    return mySingleDescriptor;
  }

  @Override
  public RefEntity getContainingFileLocalEntity() {
    final RefEntity element = getElement();
    return element instanceof RefElement && !(element instanceof RefDirectory)
           ? element
           : super.getContainingFileLocalEntity();
  }

  @Override
  protected void visitProblemSeverities(TObjectIntHashMap<HighlightDisplayLevel> counter) {
    if (!isExcluded() && isLeaf() && !getPresentation().isProblemResolved(getElement()) && !getPresentation().isSuppressed(getElement())) {
      HighlightSeverity severity = InspectionToolPresentation.getSeverity(getElement(), null, getPresentation());
      HighlightDisplayLevel level = HighlightDisplayLevel.find(severity);
      if (!counter.adjustValue(level, 1)) {
        counter.put(level, 1);
      }
      return;
    }
    super.visitProblemSeverities(counter);
  }

  @Override
  public boolean isQuickFixAppliedFromView() {
    return isLeaf() && getPresentation().isProblemResolved(getElement());
  }

  @Nullable
  @Override
  public String getTailText() {
    if (getPresentation().isDummy()) {
      return "";
    }
    final String customizedText = super.getTailText();
    if (customizedText != null) {
      return customizedText;
    }
    return isLeaf() ? "" : null;
  }

  private void checkHasDescriptorUnder(MutableTreeNode newChild) {
    if (myHasDescriptorsUnder) return;
    if (newChild instanceof ProblemDescriptionNode ||
        newChild instanceof RefElementNode && ((RefElementNode)newChild).hasDescriptorsUnder()) {
      myHasDescriptorsUnder = true;
      TreeNode parent = getParent();
      while (parent instanceof RefElementNode) {
        ((RefElementNode)parent).myHasDescriptorsUnder = true;
        parent = parent.getParent();
      }
    }
  }
}
