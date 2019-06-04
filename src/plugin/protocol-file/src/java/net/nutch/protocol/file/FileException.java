/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol.file;

import net.nutch.protocol.ProtocolException;

public class FileException extends ProtocolException {

  public FileException() {
    super();
  }

  public FileException(String message) {
    super(message);
  }

  public FileException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileException(Throwable cause) {
    super(cause);
  }

}
