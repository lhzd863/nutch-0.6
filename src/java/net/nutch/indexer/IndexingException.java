/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

public class IndexingException extends Exception {

  public IndexingException() {
    super();
  }

  public IndexingException(String message) {
    super(message);
  }

  public IndexingException(String message, Throwable cause) {
    super(message, cause);
  }

  public IndexingException(Throwable cause) {
    super(cause);
  }

}
