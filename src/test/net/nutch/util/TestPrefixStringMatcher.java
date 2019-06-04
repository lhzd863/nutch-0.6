/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import junit.framework.TestCase;

/** Unit tests for PrefixStringMatcher. */
public class TestPrefixStringMatcher extends TestCase {
  public TestPrefixStringMatcher(String name) { 
    super(name); 
  }

  private final static int NUM_TEST_ROUNDS= 20;
  private final static int MAX_TEST_PREFIXES= 100;
  private final static int MAX_PREFIX_LEN= 10;
  private final static int NUM_TEST_INPUTS_PER_ROUND= 100;
  private final static int MAX_INPUT_LEN= 20;

  private final static char[] alphabet= 
    new char[] {
      'a', 'b', 'c', 'd',
//      'e', 'f', 'g', 'h', 'i', 'j',
//      'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
//      'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4',
//      '5', '6', '7', '8', '9', '0'
    };

  private String makeRandString(int minLen, int maxLen) {
    int len= minLen + (int) (Math.random() * (maxLen - minLen));
    char[] chars= new char[len];
    
    for (int pos= 0; pos < len; pos++) {
      chars[pos]= alphabet[(int) (Math.random() * alphabet.length)];
    }
    
    return new String(chars);
  }
  
  public void testPrefixMatcher() {
    int numMatches= 0;
    int numInputsTested= 0;

    for (int round= 0; round < NUM_TEST_ROUNDS; round++) {

      // build list of prefixes
      int numPrefixes= (int) (Math.random() * MAX_TEST_PREFIXES);
      String[] prefixes= new String[numPrefixes];
      for (int i= 0; i < numPrefixes; i++) {
        prefixes[i]= makeRandString(0, MAX_PREFIX_LEN);
      }

      PrefixStringMatcher prematcher= new PrefixStringMatcher(prefixes);

      // test random strings for prefix matches
      for (int i= 0; i < NUM_TEST_INPUTS_PER_ROUND; i++) {
        String input= makeRandString(0, MAX_INPUT_LEN);
        boolean matches= false;
        int longestMatch= -1;
        int shortestMatch= -1;

        for (int j= 0; j < prefixes.length; j++) {

          if ((prefixes[j].length() > 0) 
              && input.startsWith(prefixes[j])) {

            matches= true;
            int matchSize= prefixes[j].length();

            if (matchSize > longestMatch) 
              longestMatch= matchSize;

            if ( (matchSize < shortestMatch)
                 || (shortestMatch == -1) )
              shortestMatch= matchSize;
          }

        }

        if (matches) 
          numMatches++;

        numInputsTested++;

        assertTrue( "'" + input + "' should " + (matches ? "" : "not ") 
                    + "match!",
                    matches == prematcher.matches(input) );
        if (matches) {
          assertTrue( shortestMatch 
                      == prematcher.shortestMatch(input).length());
          assertTrue( input.substring(0, shortestMatch).equals(
                        prematcher.shortestMatch(input)) );

          assertTrue( longestMatch 
                      == prematcher.longestMatch(input).length());
          assertTrue( input.substring(0, longestMatch).equals(
                        prematcher.longestMatch(input)) );

        }
      }
    }

    System.out.println("got " + numMatches + " matches out of " 
                       + numInputsTested + " tests");
  }

}
