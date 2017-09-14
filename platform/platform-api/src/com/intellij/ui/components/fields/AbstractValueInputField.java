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
package com.intellij.ui.components.fields;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.ui.components.ValidatingTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.DocumentEvent;

public abstract class AbstractValueInputField<T> extends ValidatingTextField {

  private @NotNull T myDefaultValue;
  private @Nullable String myName;

  public AbstractValueInputField(@Nullable String name, @NotNull T defaultValue) {
    myName = name;
    myDefaultValue = defaultValue;
  }

  public AbstractValueInputField(@NotNull T defaultValue) {
    this(null, defaultValue);
  }

  @Override
  public void setName(@Nullable String name) {
    myName = name;
  }

  @Override
  protected String validateTextOnChange(String text, DocumentEvent e) {
    try {
      parseValue(text);
      return null;
    }
    catch (InvalidDataException ex) {
      return ex.getMessage();
    }
  }

  @NotNull
  protected abstract T parseValue(@Nullable  String text);

  protected abstract String valueToString(@NotNull T value);

  protected abstract void assertValid(@NotNull T value);

  public void validateContent() throws ConfigurationException {
    try {
      parseValue(getText());
    }
    catch (InvalidDataException ex) {
      throw new ConfigurationException((myName != null ? myName + " " : "") + ex.getMessage());
    }
  }

  @NotNull
  public T getValue() {
    try {
      return parseValue(getText());
    }
    catch (InvalidDataException ex) {
      return myDefaultValue;
    }
  }

  public void setValue(@NotNull T newValue) {
    if (!newValue.equals(myDefaultValue)) assertValid(newValue);
    setText(valueToString(newValue));
  }

  public void setDefaultValue(@NotNull T defaultValue) {
    myDefaultValue = defaultValue;
  }

  @NotNull
  public T getDefaultValue() {
    return myDefaultValue;
  }
}
