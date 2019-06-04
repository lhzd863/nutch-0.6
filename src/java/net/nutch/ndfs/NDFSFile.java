/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

import java.io.*;
import java.net.*;
import java.util.*;


/*****************************************************************
 * NDFSFile is a traditional java File that's been annotated with
 * some extra information.
 *
 * @author Mike Cafarella
 *****************************************************************/
public class NDFSFile extends File {
    NDFSFileInfo info;

    /**
     */
    public NDFSFile(NDFSFileInfo info) {
        super(info.getPath());
        this.info = info;
    }

    /**
     * A number of File methods are unsupported in this subclass
     */
    public boolean canRead() {
        return false;
    }
    public boolean canWrite() {
        return false;
    }
    public boolean createNewFile() {
        return false;
    }
    public boolean delete() {
        return false;
    }
    public void deleteOnExit() {
    }
    public boolean isHidden() {
        return false;
    }

    /**
     * We need to reimplement some of them
     */
    public boolean isDirectory() {
        return info.isDir();
    }
    public boolean isFile() {
        return ! isDirectory();
    }
    public long length() {
        return info.getLen();
    }

    /**
     * And add a few extras
     */
    public long getContentsLength() {
        return info.getContentsLen();
    }
}
