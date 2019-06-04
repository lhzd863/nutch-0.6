/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.fs;

import java.io.*;

/****************************************************************
 * NFSOutputStream is an OutputStream that can track its position.
 *
 * @author Mike Cafarella
 *****************************************************************/
public abstract class NFSOutputStream extends OutputStream {
    /**
     * Return the current offset from the start of the file
     */
    public abstract long getPos() throws IOException;
}
