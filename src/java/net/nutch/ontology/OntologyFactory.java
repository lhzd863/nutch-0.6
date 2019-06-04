/* Copyright (c) 2004 michael j pan.  All rights reserved.   */
/* Use subject to the same conditions as Nutch. */
/* see http://www.nutch.org/LICENSE.txt. */

package net.nutch.ontology;

import net.nutch.plugin.*;
import net.nutch.util.NutchConf;
import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

/**
 * A factory for retrieving {@link Ontology} extensions.
 *
 * @author Michael Pan
 * @version $Id: OntologyFactory.java,v 1.1 2004/11/30 06:36:00 johnnx Exp $
 */
public class OntologyFactory {
  public final static Logger LOG =
    LogFormatter.getLogger(OntologyFactory.class.getName());

  private final static ExtensionPoint X_POINT =
    PluginRepository.getInstance().getExtensionPoint(Ontology.X_POINT_ID);

  private OntologyFactory() {}

  /**
  * @return Returns the online ontology extension specified
  * in nutch configuration's key
  * <code>extension.ontology.extension-name</code>.
  * If the name is  empty (no preference),
  * the first available ontology extension is returned.
  */
  public static Ontology getOntology() throws PluginRuntimeException {

    if (X_POINT == null) {
      // not even an extension point defined.
      return null;
    }

    String extensionName = NutchConf.get("extension.ontology.extension-name");
    if (extensionName != null) {
      Extension extension = findExtension(extensionName);
      if (extension != null) {
        LOG.info("Using ontology extension: " + extensionName);
        return (Ontology) extension.getExtensionInstance();
      }
      LOG.warning("Ontology extension not found: '" + extensionName 
        + "', trying the default");
      // not found, fallback to the default, if available.
    }

    Extension[] extensions = X_POINT.getExtentens();
    if (extensions.length > 0) {
      LOG.info("Using the first ontology extension found: "
        + extensions[0].getId());
      return (Ontology) extensions[0].getExtensionInstance();
    } else {
      return null;
    }

  }

  private static Extension findExtension(String name)
    throws PluginRuntimeException {

    Extension[] extensions = X_POINT.getExtentens();

    for (int i = 0; i < extensions.length; i++) {
      Extension extension = extensions[i];

      if (name.equals(extension.getId()))
        return extension;
    }

    return null;
  }

} 
