/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.fs;

import java.io.*;

/****************************************************************
 * NFSInputStream is a generic old InputStream with a little bit
 * of RAF-style seek ability.
 *
 * @author Mike Cafarella
 *****************************************************************/
public abstract class NFSInputStream extends InputStream {
    /**
     * Seek to the given offset from the start of the file.
     * The next read() will be from that location.  Can't
     * seek past the end of the file.
     */
    public abstract void seek(long pos) throws IOException;

    /**
     * Return the current offset from the start of the file
     */
    public abstract long getPos() throws IOException;
}
