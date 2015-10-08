/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.intellij.diff.comparison;

import com.intellij.diff.comparison.ByLine.Line;
import com.intellij.diff.comparison.iterables.DiffIterableUtil;
import com.intellij.diff.comparison.iterables.FairDiffIterable;
import com.intellij.diff.util.Range;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.diff.comparison.TrimUtil.expand;
import static com.intellij.diff.comparison.iterables.DiffIterableUtil.diff;
import static com.intellij.diff.comparison.iterables.DiffIterableUtil.fair;

/*
 * Base class for two-step diff algorithms.
 * Given matching between some sub-sequences of base sequences - build matching on whole base sequences
 */
abstract class ChangeCorrector {
  private final int myLength1;
  private final int myLength2;
  @NotNull private final FairDiffIterable myChanges;

  @NotNull protected final ProgressIndicator myIndicator;

  @NotNull protected final DiffIterableUtil.ChangeBuilder myBuilder;

  public ChangeCorrector(int length1,
                         int length2,
                         @NotNull FairDiffIterable changes,
                         @NotNull ProgressIndicator indicator) {
    myLength1 = length1;
    myLength2 = length2;
    myChanges = changes;
    myIndicator = indicator;

    myBuilder = new DiffIterableUtil.ChangeBuilder(length1, length2);
  }

  @NotNull
  public FairDiffIterable build() {
    execute();
    return fair(myBuilder.finish());
  }

  private int offset1 = 0;
  private int offset2 = 0;

  protected void execute() {
    for (Range ch : myChanges.iterateUnchanged()) {
      int count = ch.end1 - ch.start1;
      for (int i = 0; i < count; i++) {
        matchedPair(getOriginalIndex1(ch.start1 + i), getOriginalIndex2(ch.start2 + i));
      }
    }
    matchGap(offset1, myLength1, offset2, myLength2);
  }

  private void matchedPair(int off1, int off2) {
    matchGap(offset1, off1, offset2, off2);
    myBuilder.markEqual(off1, off2);
    offset1 = off1 + 1;
    offset2 = off2 + 1;
  }

  // match elements in range [start1 - end1) -> [start2 - end2)
  protected abstract void matchGap(int start1, int end1, int start2, int end2);

  protected abstract int getOriginalIndex1(int index);

  protected abstract int getOriginalIndex2(int index);

  //
  // Implementations
  //

  public static class DefaultCharChangeCorrector extends ChangeCorrector {
    @NotNull private final ByChar.CharOffsets myChars1;
    @NotNull private final ByChar.CharOffsets myChars2;
    @NotNull private final CharSequence myText1;
    @NotNull private final CharSequence myText2;

    public DefaultCharChangeCorrector(@NotNull ByChar.CharOffsets chars1,
                                      @NotNull ByChar.CharOffsets chars2,
                                      @NotNull CharSequence text1,
                                      @NotNull CharSequence text2,
                                      @NotNull FairDiffIterable changes,
                                      @NotNull ProgressIndicator indicator) {
      super(text1.length(), text2.length(), changes, indicator);
      myChars1 = chars1;
      myChars2 = chars2;
      myText1 = text1;
      myText2 = text2;
    }

    protected void matchGap(int start1, int end1, int start2, int end2) {
      Range expand = expand(myText1, myText2, start1, start2, end1, end2);

      CharSequence inner1 = myText1.subSequence(expand.start1, expand.end1);
      CharSequence inner2 = myText2.subSequence(expand.start2, expand.end2);
      FairDiffIterable innerChanges = ByChar.compare(inner1, inner2, myIndicator);

      myBuilder.markEqual(start1, start2, expand.start1, expand.start2);

      for (Range chunk : innerChanges.iterateUnchanged()) {
        myBuilder.markEqual(expand.start1 + chunk.start1, expand.start2 + chunk.start2, chunk.end1 - chunk.start1);
      }

      myBuilder.markEqual(expand.end1, expand.end2, end1, end2);
    }

    @Override
    protected int getOriginalIndex1(int index) {
      return myChars1.offsets[index];
    }

    @Override
    protected int getOriginalIndex2(int index) {
      return myChars2.offsets[index];
    }
  }

  public static class SmartLineChangeCorrector extends ChangeCorrector {
    @NotNull private final List<ByLine.LineWrapper> myNewLines1;
    @NotNull private final List<ByLine.LineWrapper> myNewLines2;
    @NotNull private final List<Line> myLines1;
    @NotNull private final List<Line> myLines2;

    public SmartLineChangeCorrector(@NotNull List<ByLine.LineWrapper> newLines1,
                                    @NotNull List<ByLine.LineWrapper> newLines2,
                                    @NotNull List<Line> lines1,
                                    @NotNull List<Line> lines2,
                                    @NotNull FairDiffIterable changes,
                                    @NotNull ProgressIndicator indicator) {
      super(lines1.size(), lines2.size(), changes, indicator);
      myNewLines1 = newLines1;
      myNewLines2 = newLines2;
      myLines1 = lines1;
      myLines2 = lines2;
    }

    protected void matchGap(int start1, int end1, int start2, int end2) {
      Range expand = expand(myLines1, myLines2, start1, start2, end1, end2);

      List<Line> inner1 = myLines1.subList(expand.start1, expand.end1);
      List<Line> inner2 = myLines2.subList(expand.start2, expand.end2);
      FairDiffIterable innerChanges = diff(inner1, inner2, myIndicator);

      myBuilder.markEqual(start1, start2, expand.start1, expand.start2);

      for (Range chunk : innerChanges.iterateUnchanged()) {
        myBuilder.markEqual(expand.start1 + chunk.start1, expand.start2 + chunk.start2, chunk.end1 - chunk.start1);
      }

      myBuilder.markEqual(expand.end1, expand.end2, end1, end2);
    }

    @Override
    protected int getOriginalIndex1(int index) {
      return myNewLines1.get(index).getOriginalIndex();
    }

    @Override
    protected int getOriginalIndex2(int index) {
      return myNewLines2.get(index).getOriginalIndex();
    }
  }
}
