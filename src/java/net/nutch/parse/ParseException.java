/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.parse;

public class ParseException extends Exception {

  public ParseException() {
    super();
  }

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }

}
