/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol;

import java.io.IOException;

/** A retriever of url content.  Implemented by protocol extensions. */
public interface Protocol {
  /** The name of the extension point. */
  public final static String X_POINT_ID = Protocol.class.getName();

  /** Returns the {@link Content} for a url.
   * @throws IOException for any errors.
   */
  Content getContent(String url) throws ProtocolException;
}
