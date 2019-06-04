/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol;

import java.net.URL;

/** Thrown by {@link Protocol#getContent(String)}.*/
public class ProtocolException extends Exception {

  public ProtocolException() {
    super();
  }

  public ProtocolException(String message) {
    super(message);
  }

  public ProtocolException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProtocolException(Throwable cause) {
    super(cause);
  }

}
