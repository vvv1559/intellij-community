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
package com.intellij.psi.impl;

import com.intellij.lang.jvm.JvmTypeDeclaration;
import com.intellij.lang.jvm.JvmTypeParameter;
import com.intellij.lang.jvm.types.JvmSubstitutor;
import com.intellij.lang.jvm.types.JvmType;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JvmPsiConversionHelperImpl implements JvmPsiConversionHelper {

  @Override
  public PsiClass convertTypeDeclaration(@Nullable JvmTypeDeclaration typeDeclaration) {
    if (typeDeclaration instanceof PsiClass) return (PsiClass)typeDeclaration;
    throw new RuntimeException("TODO");
  }

  @NotNull
  @Override
  public PsiTypeParameter convertTypeParameter(@NotNull JvmTypeParameter typeParameter) {
    if (typeParameter instanceof PsiTypeParameter) return (PsiTypeParameter)typeParameter;
    throw new RuntimeException("TODO");
  }

  @Nullable
  @Override
  public PsiType convertType(@Nullable JvmType type) {
    if (type == null) return null;
    if (type instanceof PsiType) return (PsiType)type;
    throw new RuntimeException("TODO");
  }

  @NotNull
  @Override
  public PsiSubstitutor convertSubstitutor(@NotNull JvmSubstitutor substitutor) {
    if (substitutor instanceof PsiJvmSubstitutor) return ((PsiJvmSubstitutor)substitutor).getPsiSubstitutor();
    PsiSubstitutor result = PsiSubstitutor.EMPTY;
    for (JvmTypeParameter parameter : substitutor.getTypeParameters()) {
      final PsiTypeParameter psiTypeParameter = convertTypeParameter(parameter);
      final PsiType psiType = convertType(substitutor.substitute(parameter));
      result = result.put(psiTypeParameter, psiType);
    }
    return result;
  }
}
