/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.*;
import junit.framework.TestCase;
import java.util.Arrays;
import net.nutch.analysis.NutchAnalysis;

public class TestQuery extends TestCase {
  public TestQuery(String name) { super(name); }

  public void testRequiredTerm() throws Exception {
    Query query = new Query();
    query.addRequiredTerm("bobo");
    testQuery(query, "bobo");
  }

  public void testProhibitedTerm() throws Exception {
    Query query = new Query();
    query.addProhibitedTerm("bobo");
    testQuery(query, "-bobo");
  }

  public void testRequiredPhrase() throws Exception {
    Query query = new Query();
    query.addRequiredPhrase(new String[] {"bobo", "bogo"});
    testQuery(query, "\"bobo bogo\"");
  }

  public void testProhibitedPhrase() throws Exception {
    Query query = new Query();
    query.addProhibitedPhrase(new String[] {"bobo", "bogo"});
    testQuery(query, "-\"bobo bogo\"");
  }

  public void testComplex() throws Exception {
    Query query = new Query();
    query.addRequiredTerm("bobo");
    query.addProhibitedTerm("bono");
    query.addRequiredPhrase(new String[] {"bobo", "bogo"});
    query.addProhibitedPhrase(new String[] {"bogo", "bobo"});
    testQuery(query, "bobo -bono \"bobo bogo\" -\"bogo bobo\"");
  }

  public static void testQuery(Query query, String string) throws Exception {
    testQueryToString(query, string);
    testQueryParser(query, string);
    testQueryIO(query, string);
  }

  public static void testQueryToString(Query query, String string) {
    assertEquals(query.toString(), string);
  }

  public static void testQueryParser(Query query, String string)
    throws Exception {
    Query after = NutchAnalysis.parseQuery(string);
    assertEquals(after, query);
    assertEquals(after.toString(), string);
  }

  public static void testQueryIO(Query query, String string) throws Exception {
    ByteArrayOutputStream oBuf = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(oBuf);
    query.write(out);

    ByteArrayInputStream iBuf = new ByteArrayInputStream(oBuf.toByteArray());
    DataInputStream in = new DataInputStream(iBuf);

    Query after = Query.read(in);

    assertEquals(after, query);
  }

  public void testQueryTerms() throws Exception {
    testQueryTerms("foo bar", new String[] {"foo", "bar"});
    testQueryTerms("\"foo bar\"", new String[] {"foo", "bar"});
    testQueryTerms("\"foo bar\" baz", new String[] {"foo", "bar", "baz"});
  }

  public static void testQueryTerms(String query, String[] terms)
    throws Exception {
    assertTrue(Arrays.equals(NutchAnalysis.parseQuery(query).getTerms(),
                             terms));
  }

  public static void main(String[] args) throws Exception {
    TestQuery test = new TestQuery("test");
    test.testComplex();
  }

}
