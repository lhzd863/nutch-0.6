/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.clustering.carrot2;

import net.nutch.searcher.HitDetails;

import com.dawidweiss.carrot.core.local.clustering.RawDocument;
import com.dawidweiss.carrot.core.local.clustering.RawDocumentBase;

/**
 * An adapter class that implements {@link RawDocument} for
 * Carrot2.  
 *
 * @author Dawid Weiss
 * @version $Id: NutchDocument.java,v 1.2 2004/08/10 00:18:43 johnnx Exp $
 */
public class NutchDocument extends RawDocumentBase {

  private final Integer id;
  
  /**
   * Creates a new document with the given id, <code>summary</code> and wrapping
   * a <code>details</code> hit details.
   */
  public NutchDocument(int id, HitDetails details, String summary) {
    super.setProperty(RawDocument.PROPERTY_URL, details.getValue("url"));
    super.setProperty(RawDocument.PROPERTY_SNIPPET, summary);
    
    String title = details.getValue("title");
    if (title != null && !"".equals(title)) {
      super.setProperty(RawDocument.PROPERTY_TITLE, title);
    }
    
    this.id = new Integer(id);
  }
  
  /*
   * @see com.dawidweiss.carrot.core.local.clustering.RawDocument#getId()
   */
  public Object getId() {
    return id;
  }
}
