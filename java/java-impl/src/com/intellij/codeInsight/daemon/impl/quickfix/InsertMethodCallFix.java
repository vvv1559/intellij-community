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
package com.intellij.codeInsight.daemon.impl.quickfix;

import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class InsertMethodCallFix implements IntentionAction, LowPriorityAction {
  private final PsiMethodCallExpression myCall;
  private final String myMethodName;

  public InsertMethodCallFix(PsiMethodCallExpression call, PsiMethod method) {
    myCall = call;
    myMethodName = method.getName();
  }

  @Nls
  @NotNull
  @Override
  public String getText() {
    return QuickFixBundle.message("insert.sam.method.call.fix.name", myMethodName);
  }

  @Nls
  @NotNull
  @Override
  public String getFamilyName() {
    return QuickFixBundle.message("insert.sam.method.call.fix.family.name");
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return myCall.isValid() && PsiManager.getInstance(project).isInProject(myCall);
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PsiExpression methodExpression = myCall.getMethodExpression();
    PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
    String replacement = methodExpression.getText() + "." + myMethodName;
    methodExpression.replace(factory.createExpressionFromText(replacement, methodExpression));
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
