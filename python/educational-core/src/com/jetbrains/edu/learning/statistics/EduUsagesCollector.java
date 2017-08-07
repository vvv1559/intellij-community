/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.jetbrains.edu.learning.statistics;

import com.intellij.internal.statistic.CollectUsagesException;
import com.intellij.internal.statistic.UsagesCollector;
import com.intellij.internal.statistic.beans.GroupDescriptor;
import com.intellij.internal.statistic.beans.UsageDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.containers.hash.HashSet;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EduUsagesCollector extends UsagesCollector {
  private static final String GROUP_ID = "educational";

  private final TObjectIntHashMap<String> myUsageDescriptors = new TObjectIntHashMap<>();

  public static void projectTypeCreated(@NotNull String projectTypeId) {
    advanceKey("project.created." + projectTypeId);
  }

  public static void projectTypeOpened(@NotNull String projectTypeId) {
    advanceKey("project.opened." + projectTypeId);
  }

  public static void taskChecked() {
    advanceKey("checkTask.");
  }

  public static void hintShown() {
    advanceKey("showHint.");
  }

  public static void taskNavigation() {
    advanceKey("navigateToTask.");
  }

  public static void courseUploaded() {
    advanceKey("uploadCourse.");
  }

  public static void createdCourseArchive() {
    advanceKey("courseArchive.");
  }

  @NotNull
  @Override
  public Set<UsageDescriptor> getUsages() throws CollectUsagesException {
    HashSet<UsageDescriptor> descriptors = new HashSet<>();
    myUsageDescriptors.forEachEntry((key, value) -> {
      descriptors.add(new UsageDescriptor(key, value));
      return true;
    });
    myUsageDescriptors.clear();
    return descriptors;
  }


  @NotNull
  @Override
  public GroupDescriptor getGroupId() {
    return GroupDescriptor.create(GROUP_ID);
  }

  private static void advanceKey(@NotNull String key) {
    TObjectIntHashMap<String> descriptors = ServiceManager.getService(EduUsagesCollector.class).myUsageDescriptors;
    int oldValue = descriptors.get(key);
    descriptors.put(key, oldValue + 1);
  }
}
