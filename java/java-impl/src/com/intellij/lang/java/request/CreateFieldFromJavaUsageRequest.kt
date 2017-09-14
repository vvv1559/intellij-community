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
package com.intellij.lang.java.request

import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageUtils.guessExpectedTypes
import com.intellij.lang.jvm.JvmModifier
import com.intellij.lang.jvm.actions.CreateFieldRequest
import com.intellij.lang.jvm.actions.ExpectedTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.createSmartPointer

internal class CreateFieldFromJavaUsageRequest(
  reference: PsiReferenceExpression,
  override val modifiers: Collection<JvmModifier>,
  override val constant: Boolean,
  private val useAnchor: Boolean
) : CreateFieldRequest {

  private val myReference = reference.createSmartPointer()

  override val isValid: Boolean get() = myReference.element?.referenceName != null

  val reference: PsiReferenceExpression get() = myReference.element!!

  val anchor: PsiElement? get() = if (useAnchor) reference else null

  override val fieldName: String get() = reference.referenceName!!

  override val fieldType: ExpectedTypes get() = guessExpectedTypes(reference, false).map(::ExpectedJavaType)
}
