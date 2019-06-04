/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol.ftp;

/** Thrown for Ftp error codes.
 */
public class FtpError extends FtpException {

  private int code;
  
  public int getCode(int code) { return code; }

  public FtpError(int code) {
    super("Ftp Error: " + code);
    this.code = code;
  }

}
