/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.clustering;

import net.nutch.searcher.HitDetails;

/**
 * An extension point interface for online search results clustering
 * algorithms.
 *
 * <p>By the term <b>online</b> search results clustering we will understand
 * a clusterer that works on a set of {@link Hit}s retrieved for a user's query
 * and produces a set of {@link Clusters} that can be displayed to help
 * the user gain insight in the topics found in the result.</p>
 *
 * <p>Other clustering options include predefined categories and off-line
 * preclustered groups, but I do not investigate those any further here.</p>
 *
 * @author Dawid Weiss
 * @version $Id: OnlineClusterer.java,v 1.1 2004/08/09 23:23:52 johnnx Exp $
 */
public interface OnlineClusterer {
  /** The name of the extension point. */
  public final static String X_POINT_ID = OnlineClusterer.class.getName();

  /**
   * Clusters an array of hits ({@link HitDetails} objects) and
   * their previously extracted summaries (<code>String</code>s).
   * 
   * <p>Arguments to this method may seem to be very low-level, but
   * in fact they are side products of a regular search process, 
   * so we simply reuse them instead of duplicating part of the usual
   * Nutch functionality. Other ideas are welcome.</p>
   * 
   * <p>This method must be thread-safe (many threads may invoke
   * it concurrently on the same instance of a clusterer).</p>
   * 
   * @return A set of {@link HitsCluster} objects.
   */
  public HitsCluster [] clusterHits(HitDetails [] hitDetails, String [] descriptions);
}
