/* Copyright (c) 2004 michael j pan.  All rights reserved.   */
/* Use subject to the same conditions as Nutch. */
/* see http://www.nutch.org/LICENSE.txt. */

package net.nutch.ontology;

import net.nutch.protocol.ProtocolFactory;
import net.nutch.protocol.Protocol;
import net.nutch.protocol.Content;
import net.nutch.protocol.ProtocolException;

import net.nutch.parse.ParserFactory;
import net.nutch.parse.Parser;
import net.nutch.parse.Parse;
import net.nutch.parse.ParseException;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import java.lang.Exception;

/** 
 * Unit tests for Ontology
 * 
 * @author michael j pan
 */
public class TestOntology extends TestCase {

  private String fileSeparator = System.getProperty("file.separator");
  // This system property is defined in ./src/plugin/build-plugin.xml
  private String sampleDir = System.getProperty("test.data",".");
  // Make sure sample files are copied to "test.data" as specified in
  // ./src/plugin/ontology/build.xml during plugin compilation.
  // Check ./src/plugin/ontology/sample/README.txt for what they are.
  private String[] sampleFiles = {"time.owl"};

  private static Ontology ontology;

  public TestOntology(String name) { 
    super(name); 
  }

  protected void setUp() {}

  protected void tearDown() {}

  public void testIt() throws ProtocolException, ParseException, Exception {
    String className = "Season";
    String[] subclassNames =
      new String[] {"Spring", "Summer", "Fall", "Winter"};

    if (ontology==null) {
      try {
        ontology = OntologyFactory.getOntology();
      } catch (Exception e) {
        throw new Exception("Failed to instantiate ontology");
      }
    }

    //foreach sample file
    for (int i=0; i<sampleFiles.length; i++) {
      //construct the url
      String urlString = "file:" + sampleDir + fileSeparator + sampleFiles[i];

      ontology.load(new String[] {urlString});

      List subclassList = new LinkedList();
  
      Iterator iter = ontology.subclasses(className);
      while (iter.hasNext()) {
        String subclassLabel = (String) iter.next();
        System.out.println(subclassLabel);
        subclassList.add(subclassLabel);
      }
  
      for (int j=0; j<subclassNames.length; j++) {
        assertTrue(subclassList.contains(subclassNames[j]));
      }
    }

  }

}
