/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

import net.nutch.io.*;
import net.nutch.util.*;

import java.io.*;
import java.util.*;

/**************************************************
 * DatanodeInfo tracks stats on a given node
 *
 * @author Mike Cafarella
 **************************************************/
public class DatanodeInfo implements Writable, Comparable {
    UTF8 name;
    long capacity, remaining, lastUpdate, lastObsoleteCheck;
    volatile TreeSet blocks;

    /**
     */
    public DatanodeInfo() {
        this(new UTF8(), 0, 0);
    }

    public DatanodeInfo(UTF8 name) {
        this.name = name;
        int colon = name.toString().indexOf(":");
        this.blocks = new TreeSet();
        this.lastObsoleteCheck = System.currentTimeMillis();
        updateHeartbeat(0, 0);        
    }

    /**
     */
    public DatanodeInfo(UTF8 name, long capacity, long remaining) {
        this.name = name;
        this.blocks = new TreeSet();
        this.lastObsoleteCheck = System.currentTimeMillis();
        updateHeartbeat(capacity, remaining);
    }

    /**
     */
    public void updateBlocks(Block newBlocks[]) {
        blocks.clear();
        for (int i = 0; i < newBlocks.length; i++) {
            blocks.add(newBlocks[i]);
        }
    }

    /**
     */
    public void addBlock(Block b) {
        blocks.add(b);
    }

    /**
     */
    public void updateHeartbeat(long capacity, long remaining) {
        this.capacity = capacity;
        this.remaining = remaining;
        this.lastUpdate = System.currentTimeMillis();
    }
    public UTF8 getName() {
        return name;
    }
    public String toString() {
        return name.toString();
    }
    public Block[] getBlocks() {
        return (Block[]) blocks.toArray(new Block[blocks.size()]);
    }
    public Iterator getBlockIterator() {
        return blocks.iterator();
    }
    public long getCapacity() {
        return capacity;
    }
    public long getRemaining() {
        return remaining;
    }
    public long lastUpdate() {
        return lastUpdate;
    }
    public void updateObsoleteCheck() {
        this.lastObsoleteCheck = System.currentTimeMillis();
    }
    public long lastObsoleteCheck() {
        return lastObsoleteCheck;
    }

    /////////////////////////////////////////////////
    // Comparable
    /////////////////////////////////////////////////
    public int compareTo(Object o) {
        DatanodeInfo d = (DatanodeInfo) o;
        return name.compareTo(d.getName());
    }

    /////////////////////////////////////////////////
    // Writable
    /////////////////////////////////////////////////
    /**
     */
    public void write(DataOutput out) throws IOException {
        name.write(out);
        out.writeLong(capacity);
        out.writeLong(remaining);
        out.writeLong(lastUpdate);

        /**
        out.writeInt(blocks.length);
        for (int i = 0; i < blocks.length; i++) {
            blocks[i].write(out);
        }
        **/
    }

    /**
     */
    public void readFields(DataInput in) throws IOException {
        this.name = new UTF8();
        this.name.readFields(in);
        this.capacity = in.readLong();
        this.remaining = in.readLong();
        this.lastUpdate = in.readLong();

        /**
        int numBlocks = in.readInt();
        this.blocks = new Block[numBlocks];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new Block();
            blocks[i].readFields(in);
        }
        **/
    }
}

