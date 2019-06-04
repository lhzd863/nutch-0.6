/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.protocol.file;

/** Thrown for File error codes.
 */
public class FileError extends FileException {

  private int code;
  
  public int getCode(int code) { return code; }

  public FileError(int code) {
    super("File Error: " + code);
    this.code = code;
  }

}
