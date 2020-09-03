/*
 * android-plugin-host-sdk-for-locale https://github.com/twofortyfouram/android-plugin-host-sdk-for-locale
 * Copyright 2015-2016 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twofortyfouram.locale.sdk.host.model;

import android.os.Parcel;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.twofortyfouram.locale.sdk.host.test.fixture.PluginInstanceDataFixture;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public final class PluginInstanceDataTest {

    @SmallTest
    @Test
    public void getType() {
        final PluginInstanceData data = PluginInstanceDataFixture.newDefaultPluginInstanceData();

        assertThat(data.getType(), is(PluginInstanceDataFixture.DEFAULT_TYPE));
    }

    @SmallTest
    @Test
    public void getRegistryName() {
        final PluginInstanceData data = PluginInstanceDataFixture.newDefaultPluginInstanceData();

        assertThat(data.getRegistryName(), is(PluginInstanceDataFixture.DEFAULT_REGISTRY_NAME));
    }

    @SmallTest
    @Test
    public void testGetBlurb() {
        final PluginInstanceData data = PluginInstanceDataFixture.newDefaultPluginInstanceData();

        assertThat(data.getBlurb(), is(PluginInstanceDataFixture.DEFAULT_BLURB));
    }

    @SmallTest
    @Test
    public void getSerializedBundle() {
        final PluginInstanceData data = PluginInstanceDataFixture.newDefaultPluginInstanceData();

        assertArrayEquals(PluginInstanceDataFixture.getDefaultBundle(),
                data.getSerializedBundle());
    }

    @SmallTest
    @Test
    public void getSerializedBundle_copy() {
        final PluginInstanceData data = PluginInstanceDataFixture.newDefaultPluginInstanceData();

        assertThat(data.getSerializedBundle(), not(sameInstance(data.getSerializedBundle())));
    }

    @SmallTest
    @Test
    public void equalsContract() {
        PluginInstanceData defaultData = PluginInstanceDataFixture.newDefaultPluginInstanceData();

        assertEquals(defaultData, defaultData);
        assertEquals(defaultData, PluginInstanceDataFixture
                .newDefaultPluginInstanceData());

        assertNotEquals(defaultData,
                new PluginInstanceData(PluginType.CONDITION,
                        PluginInstanceDataFixture.DEFAULT_REGISTRY_NAME,
                        PluginInstanceDataFixture.getDefaultBundle(),
                        PluginInstanceDataFixture.DEFAULT_BLURB)
        );

        assertNotEquals(defaultData,
                new PluginInstanceData(PluginInstanceDataFixture.DEFAULT_TYPE,
                        Plugin.generateRegistryName("bar", "foo"),
                        PluginInstanceDataFixture.getDefaultBundle(),
                        PluginInstanceDataFixture.DEFAULT_BLURB)
        );

        assertNotEquals(defaultData,
                new PluginInstanceData(PluginInstanceDataFixture.DEFAULT_TYPE,
                        PluginInstanceDataFixture.DEFAULT_REGISTRY_NAME, "bar".getBytes(),
                        PluginInstanceDataFixture.DEFAULT_BLURB)
        );

        assertNotEquals(defaultData,
                new PluginInstanceData(PluginInstanceDataFixture.DEFAULT_TYPE,
                        PluginInstanceDataFixture.DEFAULT_REGISTRY_NAME,
                        PluginInstanceDataFixture.getDefaultBundle(),
                        "bork")
        );
    }

    @SmallTest
    @Test
    public void toString_not_null() {
        final PluginInstanceData defaultData = PluginInstanceDataFixture
                .newDefaultPluginInstanceData();
        final String stringified = defaultData.toString();

        assertThat(stringified, notNullValue());
    }

    @SmallTest
    @Test
    public void toString_contains_type() {
        final PluginInstanceData defaultData = PluginInstanceDataFixture
                .newDefaultPluginInstanceData();
        final String stringified = defaultData.toString();
        assertThat(stringified, containsString(PluginInstanceDataFixture.DEFAULT_TYPE.name()));
    }

    @SmallTest
    @Test
    public void toString_contains_registry_name() {
        final PluginInstanceData defaultData = PluginInstanceDataFixture
                .newDefaultPluginInstanceData();
        final String stringified = defaultData.toString();
        assertThat(stringified, containsString(PluginInstanceDataFixture.DEFAULT_REGISTRY_NAME));
    }

    @SmallTest
    @Test
    public void toString_contains_blurb() {
        final PluginInstanceData defaultData = PluginInstanceDataFixture
                .newDefaultPluginInstanceData();
        final String stringified = defaultData.toString();
        assertThat(stringified, containsString(PluginInstanceDataFixture.DEFAULT_BLURB));
    }

    @SmallTest
    @Test
    public void parcelable() {
        final Parcel parcel = Parcel.obtain();
        try {
            final PluginInstanceData data = PluginInstanceDataFixture
                    .newDefaultPluginInstanceData();

            data.writeToParcel(parcel, 0);

            parcel.setDataPosition(0);
            final PluginInstanceData unparceled = PluginInstanceData.CREATOR
                    .createFromParcel(parcel);

            assertThat(unparceled, is(data));
        } finally {
            parcel.recycle();
        }
    }
}
