package net.nutch.protocol.ftp;

/**
 * Exception indicating bad reply of SYST command.
 *
 * @author John Xing
 */
public class FtpExceptionBadSystResponse extends FtpException {
  FtpExceptionBadSystResponse(String msg) {
    super(msg);
  }
}
