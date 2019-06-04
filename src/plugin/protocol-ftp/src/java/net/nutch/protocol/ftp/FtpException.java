/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol.ftp;

import net.nutch.protocol.ProtocolException;

/***
 * Superclass for important exceptions thrown during FTP talk,
 * that must be handled with care.
 *
 * @author John Xing
 */
public class FtpException extends ProtocolException {

  public FtpException() {
    super();
  }

  public FtpException(String message) {
    super(message);
  }

  public FtpException(String message, Throwable cause) {
    super(message, cause);
  }

  public FtpException(Throwable cause) {
    super(cause);
  }

}
