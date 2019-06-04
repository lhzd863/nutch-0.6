/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.clustering.carrot2;

import java.io.File;

import net.nutch.clustering.HitsCluster;
import net.nutch.searcher.Hit;
import net.nutch.searcher.HitDetails;
import net.nutch.searcher.Hits;
import net.nutch.searcher.NutchBean;
import net.nutch.searcher.Query;
import junit.framework.TestCase;

/**
 * A test case for the Carrot2-based clusterer plugin to Nutch.
 *
 * <p><b>This test case is mostly commented-out because I don't know
 * how to integrate a test that requires an existing Nutch index.</b></p>
 *
 * @author Dawid Weiss
 * @version $Id: ClustererTest.java,v 1.1 2004/08/09 23:23:53 johnnx Exp $
 */
public class ClustererTest extends TestCase {

  public ClustererTest(String s) {
    super(s);
  }
  
  public ClustererTest() {
    super();
  }

  public void testEmptyInput() {
    Clusterer c = new Clusterer();
    
    HitDetails [] hitDetails = new HitDetails[0];
    String [] descriptions = new String [0];

    HitsCluster [] clusters = c.clusterHits(hitDetails, descriptions);
    assertTrue( clusters != null && clusters.length == 0 );
  }

  /*
  
  UNCOMMENT THIS IF YOU HAVE A NUTCH INDEX AVAILABLE. REPLACE
  THE HARDCODED PATH TO IT.
  
  public void testSomeInput() throws Exception {
    Clusterer c = new Clusterer();

    NutchBean bean = new NutchBean(
      new File("c:\\dweiss\\data\\mozdex-nutch\\nutch-mozdex\\resin"));
    Query q = Query.parse( "blog" );
    Hits hits = bean.search(q, 100);

    Hit[] show = hits.getHits(0, 100);
    HitDetails[] details = bean.getDetails(show);
    String[] summaries = bean.getSummary(details, q);

    HitsCluster [] clusters = c.clusterHits(details, summaries);
    assertTrue( clusters != null );
    
    for (int i=0;i<clusters.length;i++) {
        HitsCluster cluster = clusters[i];
        dump(0, cluster);
    }
  }    
  */
  
  private void dump(int level, HitsCluster cluster) {
    String [] labels = cluster.getDescriptionLabels();
    for (int indent = 0; indent<level; indent++) {
      System.out.print( "   " );
    }
    System.out.print(">> ");
    if (cluster.isJunkCluster()) System.out.print("(Junk) ");
    System.out.print("CLUSTER: ");
    for (int i=0;i<labels.length;i++) {
      System.out.print( labels[i] + "; " );
    }
    System.out.println();
    
    HitsCluster [] subclusters = cluster.getSubclusters();
    if (subclusters != null) {
      for (int i=0;i<subclusters.length;i++) {
        dump(level + 1, subclusters[i]);
      }
    }
    
    // dump documents.
    HitDetails [] hits = cluster.getHits();
    if (hits != null) {
      for (int i=0;i<hits.length;i++ ) {
        for (int indent = 0; indent<level; indent++) {
          System.out.print( "   " );
        }
        System.out.print( hits[i].getValue("url") );
        System.out.print( "; " );
        System.out.println( hits[i].getValue("title") );
      }
    }
  }
}
