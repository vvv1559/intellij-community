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
package com.intellij.java.propertyBased;

import com.intellij.testFramework.propertyBased.IntentionPolicy;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
class JavaIntentionPolicy extends IntentionPolicy {
  @Override
  protected boolean shouldSkipIntention(@NotNull String actionText) {
    return actionText.startsWith("Generate empty 'private' constructor") || // displays a dialog
           actionText.startsWith("Attach annotations") || // changes project model
           actionText.startsWith("Change class type parameter") || // doesn't change file text (starts live template)
           actionText.startsWith("Rename reference") || // doesn't change file text (starts live template)
           super.shouldSkipIntention(actionText);
  }
}

class JavaGreenIntentionPolicy extends JavaIntentionPolicy {
  @Override
  protected boolean shouldSkipIntention(@NotNull String actionText) {
    return actionText.startsWith("Flip") || // doesn't care about compilability
           actionText.startsWith("Convert to string literal") || // can produce uncompilable code by design
           actionText.startsWith("Detail exceptions") || // can produce uncompilable code if 'catch' section contains 'instanceof's
           actionText.startsWith("Insert call to super method") || // super method can declare checked exceptions, unexpected at this point
           actionText.startsWith("Cast to ") || // produces uncompilable code by design
           actionText.startsWith("Unwrap 'else' branch (changes semantics)") || // might produce code with final variables are initialized several times
           actionText.startsWith("Create missing 'switch' branches") || // if all existing branches do 'return something', we don't automatically generate compilable code for new branches
           actionText.startsWith("Unimplement") || // e.g. leaves red references to the former superclass methods
           super.shouldSkipIntention(actionText);
  }
}
