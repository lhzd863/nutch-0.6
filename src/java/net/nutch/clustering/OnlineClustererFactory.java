/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.clustering;

import net.nutch.plugin.*;
import net.nutch.util.NutchConf;
import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

/**
 * A factory for retrieving {@link OnlineClusterer} extensions.
 *
 * @author Dawid Weiss
 * @version $Id: OnlineClustererFactory.java,v 1.1 2004/08/09 23:23:52 johnnx Exp $
 */
public class OnlineClustererFactory {
  public static final Logger LOG = LogFormatter
    .getLogger(OnlineClustererFactory.class.getName());

  private final static ExtensionPoint X_POINT = PluginRepository.getInstance()
    .getExtensionPoint(OnlineClusterer.X_POINT_ID);

  private OnlineClustererFactory() {}

  /**
  * @return Returns the online clustering extension specified
  * in nutch configuration's key
  * <code>extension.clustering.extension-name</code>. If the name is
  * empty (no preference), the first available clustering extension is
  * returned.
  */
  public static OnlineClusterer getOnlineClusterer()
    throws PluginRuntimeException {

    if (X_POINT == null) {
      // not even an extension point defined.
      return null;
    }

    String extensionName = NutchConf.get("extension.clustering.extension-name");
    if (extensionName != null) {
      Extension extension = findExtension(extensionName);
      if (extension != null) {
        LOG.info("Using clustering extension: " + extensionName);
        return (OnlineClusterer) extension.getExtensionInstance();
      }
      LOG.warning("Clustering extension not found: '" + extensionName 
        + "', trying the default");
      // not found, fallback to the default, if available.
    }

    Extension[] extensions = X_POINT.getExtentens();
    if (extensions.length > 0) {
      LOG.info("Using the first clustering extension found: "
        + extensions[0].getId());
      return (OnlineClusterer) extensions[0].getExtensionInstance();
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
