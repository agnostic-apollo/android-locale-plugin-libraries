/*
 * android-spackle https://github.com/twofortyfouram/android-spackle
 * Copyright (C) 2009–2017 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package log;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.twofortyfouram.log.Lumberjack;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.twofortyfouram.test.matcher.ClassNotInstantiableMatcher.notInstantiable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public final class LumberjackTest {

    /*
     * Note: This test is not intended to check the actual logcat output.  By at least executing
     * the code paths, we're testing that no exceptions are thrown.
     */

    @SmallTest
    @Test
    public void nonInstantiable() {
        assertThat(Lumberjack.class, notInstantiable());
    }

    @SmallTest
    @Test
    public void log_without_format() {
        final String message = "Test message"; //$NON-NLS-1$

        Lumberjack.always(message);
        Lumberjack.v(message);
        Lumberjack.d(message);
        Lumberjack.i(message);
        Lumberjack.w(message);
        Lumberjack.e(message);
    }

    @SmallTest
    @Test
    public void log_with_format_array_object() {
        final String message = "Test message %s"; //$NON-NLS-1$
        final Object[] array = new Object[]{
                new Location("test")}; //$NON-NLS-1$

        Lumberjack.always(message, array);
        Lumberjack.v(message, array);
        Lumberjack.d(message, array);
        Lumberjack.i(message, array);
        Lumberjack.w(message, array);
        Lumberjack.e(message, array);
    }

    @SmallTest
    @Test
    public void log_with_format_array_primitive() {
        final String message = "Test message %s"; //$NON-NLS-1$
        final int[] array = new int[]{
                1, 2, 3
        };

        Lumberjack.always(message, array);
        Lumberjack.v(message, array);
        Lumberjack.d(message, array);
        Lumberjack.i(message, array);
        Lumberjack.w(message, array);
        Lumberjack.e(message, array);
    }

    @SmallTest
    @Test
    public void log_with_format_bundle() {
        final String message = "Test message %s"; //$NON-NLS-1$
        final Bundle bundle = new Bundle();

        Lumberjack.always(message, bundle);
        Lumberjack.v(message, bundle);
        Lumberjack.d(message, bundle);
        Lumberjack.i(message, bundle);
        Lumberjack.w(message, bundle);
        Lumberjack.e(message, bundle);
    }

    @SmallTest
    @Test
    public void log_with_format_int() {
        final String message = "Test message %d"; //$NON-NLS-1$

        Lumberjack.always(message, 1);
        Lumberjack.v(message, 1);
        Lumberjack.d(message, 1);
        Lumberjack.i(message, 1);
        Lumberjack.w(message, 1);
        Lumberjack.e(message, 1);
    }

    @SmallTest
    @Test
    public void log_with_format_intent() {
        final String message = "Test message"; //$NON-NLS-1$
        final Intent intent = new Intent();

        Lumberjack.always(message, intent);
        Lumberjack.v(message, intent);
        Lumberjack.d(message, intent);
        Lumberjack.i(message, intent);
        Lumberjack.w(message, intent);
        Lumberjack.e(message, intent);
    }

    @SmallTest
    @Test
    public void log_with_format_null() {
        final String message = "Test message %s"; //$NON-NLS-1$

        Lumberjack.always(message, (Object[]) null);
        Lumberjack.v(message, (Object[]) null);
        Lumberjack.d(message, (Object[]) null);
        Lumberjack.i(message, (Object[]) null);
        Lumberjack.w(message, (Object[]) null);
        Lumberjack.e(message, (Object[]) null);
    }

    @SmallTest
    @Test
    public void log_with_format_object() {
        final String message = "Test message %s"; //$NON-NLS-1$

        Lumberjack.always(message, new Object());
        Lumberjack.v(message, new Object());
        Lumberjack.d(message, new Object());
        Lumberjack.i(message, new Object());
        Lumberjack.w(message, new Object());
        Lumberjack.e(message, new Object());
    }

    @SmallTest
    @Test
    public void log_with_format_string() {
        final String message = "Test message %s"; //$NON-NLS-1$

        Lumberjack.always(message, "string"); //$NON-NLS-1$
        Lumberjack.v(message, "string");//$NON-NLS-1$
        Lumberjack.d(message, "string");//$NON-NLS-1$
        Lumberjack.i(message, "string");//$NON-NLS-1$
        Lumberjack.w(message, "string");//$NON-NLS-1$
        Lumberjack.e(message, "string");//$NON-NLS-1$
    }

    @SmallTest
    @Test
    public void log_with_format_throwable() {
        final String message = "Test message %s"; //$NON-NLS-1$
        final Exception e = new RuntimeException();

        Lumberjack.always(message, e);
        Lumberjack.v(message, e);
        Lumberjack.d(message, e);
        Lumberjack.i(message, e);
        Lumberjack.w(message, e);
        Lumberjack.e(message, e);
    }

    @SmallTest
    @Test
    public void log_with_throwable() {
        final String message = "Test message"; //$NON-NLS-1$
        final Exception e = new RuntimeException();

        Lumberjack.always(message, e);
        Lumberjack.v(message, e);
        Lumberjack.d(message, e);
        Lumberjack.i(message, e);
        Lumberjack.w(message, e);
        Lumberjack.e(message, e);
    }

    @SmallTest
    @Test
    public void formatMessage_array() {
        final String expected = "foo [bar, baz]";

        final String actual = Lumberjack.formatMessage("%s %s", "foo", new String[]{"bar, baz"});

        assertEquals(expected, actual);
    }
}
