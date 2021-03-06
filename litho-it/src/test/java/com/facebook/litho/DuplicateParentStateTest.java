/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import android.graphics.Color;

import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.yoga.YogaAlign;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(ComponentsTestRunner.class)
public class DuplicateParentStateTest {

  private int mUnspecifiedSizeSpec;

  @Before
  public void setUp() throws Exception {
    mUnspecifiedSizeSpec = SizeSpec.makeSizeSpec(0, SizeSpec.UNSPECIFIED);
  }

  @Test
  public void testDuplicateParentStateAvoidedIfRedundant() {
    final Component component = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
            .duplicateParentState(true)
            .clickHandler(c.newEventHandler(1))
            .child(
                Column.create(c)
                    .duplicateParentState(false)
                    .child(
                        TestDrawableComponent.create(c)
                            .withLayout()
                            .duplicateParentState(true)))
            .child(
                Column.create(c)
                    .duplicateParentState(true)
                    .child(
                        TestDrawableComponent.create(c)
                            .withLayout()
                            .duplicateParentState(true)))
            .child(
                Column.create(c)
                    .clickHandler(c.newEventHandler(2))
                    .child(
                        TestDrawableComponent.create(c)
                            .withLayout()
                            .duplicateParentState(true)))
            .child(
                Column.create(c)
                    .clickHandler(c.newEventHandler(3))
                    .child(
                        TestDrawableComponent.create(c)
                            .withLayout()
                            .duplicateParentState(false)))
            .child(
                Column.create(c)
                    .clickHandler(c.newEventHandler(3))
                    .backgroundColor(Color.RED)
                    .foregroundColor(Color.RED))
            .child(
                Column.create(c)
                    .backgroundColor(Color.BLUE)
                    .foregroundColor(Color.BLUE))
            .build();
      }
    };

    LayoutState layoutState = LayoutState.calculate(
        new ComponentContext(RuntimeEnvironment.application),
        component,
        -1,
        mUnspecifiedSizeSpec,
        mUnspecifiedSizeSpec,
        false,
        false,
        null);

    assertEquals(12, layoutState.getMountableOutputCount());

    assertTrue(
        "Clickable root output has duplicate state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(0).getFlags()));

    assertFalse(
        "Parent doesn't duplicate host state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(1).getFlags()));

    assertTrue(
        "Parent does duplicate host state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(2).getFlags()));

    assertTrue(
        "Drawable duplicates clickable parent state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(4).getFlags()));

    assertFalse(
        "Drawable doesn't duplicate clickable parent state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(6).getFlags()));

    assertTrue(
        "Background should duplicate clickable node state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(8).getFlags()));
    assertTrue(
        "Foreground should duplicate clickable node state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(9).getFlags()));

    assertFalse(
        "Background should duplicate non-clickable node state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(10).getFlags()));
    assertFalse(
        "Foreground should duplicate non-clickable node state",
        MountItem.isDuplicateParentState(layoutState.getMountableOutputAt(11).getFlags()));
  }
}
