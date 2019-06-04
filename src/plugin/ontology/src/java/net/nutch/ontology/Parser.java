/* Copyright (c) 2004 michael j pan.  All rights reserved.   */
/* Use subject to the same conditions as Nutch. */
/* see http://www.nutch.org/LICENSE.txt. */

package net.nutch.ontology;

import com.hp.hpl.jena.ontology.OntModel;

import java.util.Iterator;

/**
 * interface for the parser
 *
 * @author michael j pan
 */
public interface Parser {
  public void parse(OntModel m);
  public Iterator rootClasses(OntModel m);
}
