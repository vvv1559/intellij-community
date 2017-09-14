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

import com.intellij.openapi.util.InvalidDataException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommaSeparatedIntegersField extends AbstractValueInputField<List<Integer>> {

  private final int myMinValue;
  private final int myMaxValue;

  public CommaSeparatedIntegersField(int minValue, int maxValue, String optionalText) {
    super(Collections.emptyList());
    myMinValue = minValue;
    myMaxValue = maxValue;
    getEmptyText().setText(optionalText);
  }


  @NotNull
  @Override
  protected List<Integer> parseValue(@Nullable String text) {
    if (text == null || text.isEmpty()) return Collections.emptyList();
    String[] chunks = text.split("\\s*,\\s*");
    List<Integer> values = new ArrayList<>(chunks.length);
    for (String chunk : chunks) {
      try {
        int value = Integer.parseInt(chunk);
        if (value < myMinValue || value > myMaxValue) {
          throw new InvalidDataException("Value " + value + " is out of range " + myMinValue + ".." + myMaxValue);
        }
        values.add(value);
      }
      catch (NumberFormatException nfe) {
        throw new InvalidDataException("Value '" + chunk + "' is not an integer number");
      }
    }
    Collections.sort(values);
    return values;
  }

  @Override
  protected String valueToString(@NotNull List<Integer> valueList) {
    return intListToString(valueList);
  }

  @Override
  protected void assertValid(@NotNull List<Integer> valueList) {

  }

  public static String intListToString(@NotNull List<Integer> valueList) {
    StringBuilder sb = new StringBuilder();
    for (Integer value : valueList) {
      if (sb.length() != 0) sb.append(", ");
      sb.append(value);
    }
    return sb.toString();
  }

}
