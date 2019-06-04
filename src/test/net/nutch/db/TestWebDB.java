/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.db;

import net.nutch.fs.*;
import net.nutch.util.*;

import junit.framework.TestCase;
import java.io.*;

/*************************************************
 * This is the unit test for WebDBWriter and 
 * WebDBReader.  It's really not much more than
 * a nice Junit-compatible wrapper around DBTester.
 *
 *************************************************/
public class TestWebDB extends TestCase {

    /**
     * Build the TestWebDB by calling super's constructor.
     */
    public TestWebDB(String name) {
        super(name);
    }

    /**
     * The main test method, invoked by Junit.
     */
    public void testWebDB() throws Exception {
        String testDir = System.getProperty("test.build.data",".");
        File dbDir = new File(testDir, "testDb");
        dbDir.delete();
        dbDir.mkdir();

        DBTester tester = new DBTester(new LocalFileSystem(), dbDir, 500);
        tester.runTest();
        tester.cleanup();
        dbDir.delete();
    }
}
