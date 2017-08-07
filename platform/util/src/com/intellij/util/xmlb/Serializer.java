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
package com.intellij.util.xmlb;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface Serializer {
  @NotNull
  Binding getClassBinding(@NotNull Class<?> aClass, @NotNull Type originalType, @Nullable MutableAccessor accessor);

  Binding getClassBinding(@NotNull Class<?> aClass);

  @Nullable
  Binding getBinding(@NotNull MutableAccessor accessor);

  @Nullable
  Binding getBinding(@NotNull Type type);

  @Nullable
  Binding getBinding(@NotNull Class<?> aClass, @NotNull Type type);
}
