/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

import net.nutch.io.*;
import net.nutch.util.*;

import java.io.*;
import java.util.*;

/******************************************************
 * NDFSFileInfo tracks info about remote files, including
 * name, size, etc.  
 * 
 * @author Mike Cafarella
 ******************************************************/
public class NDFSFileInfo implements Writable {
    UTF8 path;
    long len;
    long contentsLen;
    boolean isDir;

    /**
     */
    public NDFSFileInfo() {
    }

    /**
     */
    public NDFSFileInfo(UTF8 path, long len, long contentsLen, boolean isDir) {
        this.path = path;
        this.len = len;
        this.contentsLen = contentsLen;
        this.isDir = isDir;
    }

    /**
     */
    public String getPath() {
        return new File(path.toString()).getPath();
    }

    /**
     */
    public String getName() {
        return new File(path.toString()).getName();
    }

    /**
     */
    public String getParent() {
        return new File(path.toString()).getParent();
    }

    /**
     */
    public long getLen() {
        return len;
    }

    /**
     */
    public long getContentsLen() {
        return contentsLen;
    }

    /**
     */
    public boolean isDir() {
        return isDir;
    }

    //////////////////////////////////////////////////
    // Writable
    //////////////////////////////////////////////////
    public void write(DataOutput out) throws IOException {
        path.write(out);
        out.writeLong(len);
        out.writeLong(contentsLen);
        out.writeBoolean(isDir);
    }

    public void readFields(DataInput in) throws IOException {
        this.path = new UTF8();
        this.path.readFields(in);
        this.len = in.readLong();
        this.contentsLen = in.readLong();
        this.isDir = in.readBoolean();
    }
}

