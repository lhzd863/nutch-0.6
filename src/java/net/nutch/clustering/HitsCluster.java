/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.clustering;

import net.nutch.searcher.HitDetails;

/**
 * An interface representing a group of hits.
 * 
 * <p>If {@link #isJunkCluster()} method returns <code>true</code>
 * then this cluster contains documents that are grouped together,
 * but no clear semantic relation has been detected; this is mostly
 * the case with "Other topics" clusters. Such clusters may
 * be discarded by the user interface layer.</p>
 *
 * @author Dawid Weiss
 * @version $Id: HitsCluster.java,v 1.1 2004/08/09 23:23:52 johnnx Exp $
 */
public interface HitsCluster {
  /**
   * @return Returns an array of {@link HitsCluster} objects
   * that are sub-groups of the current group, or <code>null</code>
   * if this cluster has no sub-groups.
   */
  public HitsCluster [] getSubclusters();
  
  /**
   * @return Returns a relevance-ordered array of the hits belonging
   * to this cluster or <code>null</code> if this cluster
   * has no associated documents (it may have subclusters only).
   */
  public HitDetails[] getHits();
  
  /**
   * @return Returns an array of labels for this cluster. The labels should
   * be sorted according to their relevance to the cluster's content. Not
   * all of the labels must be displayed - the application is free to
   * set a cutoff threshold and display only the topmost labels. 
   */
  public String[] getDescriptionLabels();
  
  /**
   * Returns <code>true</code> if this cluster constains documents
   * that did not fit anywhere else (presentation layer may
   * discard such clusters). 
   * 
   * <p>Subclusters of this cluster are also junk clusters, even if
   * they don't have this property set to <code>true</code></p>
   */
  public boolean isJunkCluster();
}
