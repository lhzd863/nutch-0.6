/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import junit.framework.TestCase;

/** Unit tests for StringUtil methods. */
public class TestStringUtil extends TestCase {
  public TestStringUtil(String name) { 
    super(name); 
  }

  public void testRightPad() {
    String s= "my string";

    String ps= StringUtil.rightPad(s, 0);
    assertTrue(s.equals(ps));

    ps= StringUtil.rightPad(s, 9);
    assertTrue(s.equals(ps));

    ps= StringUtil.rightPad(s, 10);
    assertTrue( (s+" ").equals(ps) );

    ps= StringUtil.rightPad(s, 15);
    assertTrue( (s+"      ").equals(ps) );

  }

  public void testLeftPad() {
    String s= "my string";

    String ps= StringUtil.leftPad(s, 0);
    assertTrue(s.equals(ps));

    ps= StringUtil.leftPad(s, 9);
    assertTrue(s.equals(ps));

    ps= StringUtil.leftPad(s, 10);
    assertTrue( (" "+s).equals(ps) );

    ps= StringUtil.leftPad(s, 15);
    assertTrue( ("      "+s).equals(ps) );

  }

}
