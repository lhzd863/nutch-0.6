/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol.http;

import net.nutch.protocol.ProtocolException;

public class HttpException extends ProtocolException {

  public HttpException() {
    super();
  }

  public HttpException(String message) {
    super(message);
  }

  public HttpException(String message, Throwable cause) {
    super(message, cause);
  }

  public HttpException(Throwable cause) {
    super(cause);
  }

}
