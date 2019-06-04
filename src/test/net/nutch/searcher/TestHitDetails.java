/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.*;
import net.nutch.io.*;
import junit.framework.TestCase;

public class TestHitDetails extends TestCase {
  public TestHitDetails(String name) { super(name); }

  public void testHitDetails() throws Exception {
    final int length = 3;
    final String[] fields = new String[] {"a", "b", "c" };
    final String[] values = new String[] { "foo", "bar", "baz" };

    HitDetails before = new HitDetails(fields, values);

    DataOutputBuffer dob = new DataOutputBuffer();
    before.write(dob);

    DataInputBuffer dib = new DataInputBuffer();
    dib.reset(dob.getData(), dob.getLength());

    HitDetails after = HitDetails.read(dib);

    assertEquals(length, after.getLength());
    for (int i = 0; i < length; i++) {
      assertEquals(fields[i], after.getField(i));
      assertEquals(values[i], after.getValue(i));
      assertEquals(values[i], after.getValue(fields[i]));
    }
  }
}
