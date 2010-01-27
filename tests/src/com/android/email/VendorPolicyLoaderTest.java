/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.email;

import android.content.Context;
import android.os.Bundle;
import android.test.AndroidTestCase;

public class VendorPolicyLoaderTest extends AndroidTestCase {
    /**
     * Test for the case where the helper package doesn't exist.
     */
    public void testPackageNotExist() {
        VendorPolicyLoader pl = new VendorPolicyLoader(getContext(), "no.such.package",
                "no.such.Class", true);

        // getPolicy() shouldn't throw any exception.
        assertEquals(Bundle.EMPTY, pl.getPolicy(null, null));
    }

    public void testIsSystemPackage() {
        final Context c = getContext();
        assertEquals(false, VendorPolicyLoader.isSystemPackage(c, "no.such.package"));
        assertEquals(false, VendorPolicyLoader.isSystemPackage(c, "com.android.email.tests"));
        assertEquals(true, VendorPolicyLoader.isSystemPackage(c, "com.android.settings"));
    }

    /**
     * Actually call {@link VendorPolicyLoader#getPolicy}, using MockVendorPolicy as a vendor
     * policy.
     */
    public void testGetPolicy() {
        // Because MockVendorPolicy lives in a non-system apk, we need to skip the system-apk check.
        VendorPolicyLoader pl = new VendorPolicyLoader(getContext(), "com.android.email.tests",
                MockVendorPolicy.class.getName(), true);

        // Prepare result
        Bundle result = new Bundle();
        result.putInt("ret", 1);
        MockVendorPolicy.mockResult = result;

        // Arg to pass
        Bundle args = new Bundle();
        args.putString("arg1", "a");

        // Call!
        Bundle actualResult = pl.getPolicy("policy1", args);

        // Check passed args
        assertEquals("operation", "policy1", MockVendorPolicy.passedPolicy);
        assertEquals("arg", "a", MockVendorPolicy.passedBundle.getString("arg1"));

        // Check return value
        assertEquals("result", 1, actualResult.getInt("ret"));
    }


    /**
     * Same as {@link #testGetPolicy}, but with the system-apk check.  It's a test for the case
     * where we have a non-system vendor policy installed, which shouldn't be used.
     */
    public void testGetPolicyNonSystem() {
        VendorPolicyLoader pl = new VendorPolicyLoader(getContext(), "com.android.email.tests",
                MockVendorPolicy.class.getName(), false);

        MockVendorPolicy.passedPolicy = null;

        // getPolicy() shouldn't throw any exception.
        assertEquals(Bundle.EMPTY, pl.getPolicy("policy1", null));

        // MockVendorPolicy.getPolicy() shouldn't get called.
        assertNull(MockVendorPolicy.passedPolicy);
    }

    public static class MockVendorPolicy {
        public static String passedPolicy;
        public static Bundle passedBundle;
        public static Bundle mockResult;

        public static Bundle getPolicy(String operation, Bundle args) {
            passedPolicy = operation;
            passedBundle = args;
            return mockResult;
        }
    }
}
