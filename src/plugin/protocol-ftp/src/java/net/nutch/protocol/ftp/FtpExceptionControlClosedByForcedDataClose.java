package net.nutch.protocol.ftp;

/**
 * Exception indicating control channel is closed by server end, due to
 * forced closure of data channel at client (our) end.
 *
 * @author John Xing
 */
public class FtpExceptionControlClosedByForcedDataClose extends FtpException {
  FtpExceptionControlClosedByForcedDataClose(String msg) {
    super(msg);
  }
}
