package org.chad.jeejah.library;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.chad.jeejah.library.SearchTest \
 * org.chad.jeejah.library.tests/android.test.InstrumentationTestRunner
 */
public class SearchTest extends ActivityInstrumentationTestCase2<Search> {

    public SearchTest() {
        super("org.chad.jeejah.library", Search.class);
    }

}
