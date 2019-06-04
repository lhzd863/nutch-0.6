/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;

/** 
 * A WritableComparable for booleans. 
 */
public class BooleanWritable implements WritableComparable {
    private boolean value;

    /** 
     */
    public BooleanWritable() {};

    /** 
     */
    public BooleanWritable(boolean value) {
        set(value);
    }

    /** 
     * Set the value of the BooleanWritable
     */    
    public void set(boolean value) {
        this.value = value;
    }

    /**
     * Returns the value of the BooleanWritable
     */
    public boolean get() {
        return value;
    }

    /**
     */
    public void readFields(DataInput in) throws IOException {
        value = in.readBoolean();
    }

    /**
     */
    public void write(DataOutput out) throws IOException {
        out.writeBoolean(value);
    }

    /**
     */
    public boolean equals(Object o) {
        if (!(o instanceof BooleanWritable)) {
            return false;
        }
        BooleanWritable other = (BooleanWritable) o;
        return this.value == other.value;
    }

    /**
     */
    public int compareTo(Object o) {
        boolean a = this.value;
        boolean b = ((BooleanWritable) o).value;
        return ((a == b) ? 0 : (a == false) ? -1 : 1);
    }

    /** 
     * A Comparator optimized for BooleanWritable. 
     */ 
    public static class Comparator extends WritableComparator {
        public Comparator() {
            super(BooleanWritable.class);
        }

        public int compare(byte[] b1, int s1, int l1,
                           byte[] b2, int s2, int l2) {
            boolean a = (readInt(b1, s1) == 1) ? true : false;
            boolean b = (readInt(b2, s2) == 1) ? true : false;
            return ((a == b) ? 0 : (a == false) ? -1 : 1);
        }
    }
}
