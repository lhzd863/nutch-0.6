/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

import net.nutch.util.*;
import java.util.logging.*;

/** Factory to create a UrlNormalizer from "urlnormalizer.class" config property. */
public class UrlNormalizerFactory {
  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.net.UrlNormalizerFactory");

  private static final String URLNORMALIZER_CLASS =
    NutchConf.get("urlnormalizer.class");

  private UrlNormalizerFactory() {}                   // no public ctor

  private static UrlNormalizer normalizer;

  /** Return the default UrlNormalizer implementation. */
  public static UrlNormalizer getNormalizer() {

    if (normalizer == null) {
      try {
        LOG.info("Using URL normalizer: " + URLNORMALIZER_CLASS);
        Class normalizerClass = Class.forName(URLNORMALIZER_CLASS);
        normalizer = (UrlNormalizer)normalizerClass.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Couldn't create "+URLNORMALIZER_CLASS, e);
      }
    }

    return normalizer;

  }

}
