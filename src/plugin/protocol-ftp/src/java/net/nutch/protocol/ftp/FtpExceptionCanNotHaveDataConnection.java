package net.nutch.protocol.ftp;

/**
 * Exception indicating failure of opening data connection.
 *
 * @author John Xing
 */
public class FtpExceptionCanNotHaveDataConnection extends FtpException {
  FtpExceptionCanNotHaveDataConnection(String msg) {
    super(msg);
  }
}
