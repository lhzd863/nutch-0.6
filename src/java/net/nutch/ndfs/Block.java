/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

import net.nutch.io.*;

import java.io.*;
import java.util.*;

/**************************************************
 * A Block is a Nutch FS primitive, identified by a 
 * long.
 *
 * @author Mike Cafarella
 **************************************************/
public class Block implements Writable, Comparable {
    static Random r = new Random();

    /**
     */
    public static boolean isBlockFilename(File f) {
        if (f.getName().startsWith("blk_")) {
            return true;
        } else {
            return false;
        }
    }

    long blkid;
    long len;

    /**
     */
    public Block() {
        this.blkid = r.nextLong();
        this.len = 0;
    }

    /**
     */
    public Block(long blkid, long len) {
        this.blkid = blkid;
        this.len = len;
    }

    /**
     * Find the blockid from the given filename
     */
    public Block(File f, long len) {
        String name = f.getName();
        name = name.substring("blk_".length());
        this.blkid = Long.parseLong(name);
        this.len = len;
    }

    /**
     */
    public long getBlockId() {
        return blkid;
    }

    /**
     */
    public String getBlockName() {
        return "blk_" + String.valueOf(blkid);
    }

    /**
     */
    public long getNumBytes() {
        return len;
    }
    public void setNumBytes(long len) {
        this.len = len;
    }

    /**
     */
    public String toString() {
        return getBlockName();
    }

    /////////////////////////////////////
    // Writable
    /////////////////////////////////////
    public void write(DataOutput out) throws IOException {
        out.writeLong(blkid);
        out.writeLong(len);
    }

    public void readFields(DataInput in) throws IOException {
        this.blkid = in.readLong();
        this.len = in.readLong();
    }

    /////////////////////////////////////
    // Comparable
    /////////////////////////////////////
    public int compareTo(Object o) {
        Block b = (Block) o;
        if (getBlockId() < b.getBlockId()) {
            return -1;
        } else if (getBlockId() == b.getBlockId()) {
            return 0;
        } else {
            return 1;
        }
    }
}
