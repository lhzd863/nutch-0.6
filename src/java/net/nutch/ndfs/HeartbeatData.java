/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

import net.nutch.io.*;
import net.nutch.ipc.*;
import net.nutch.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/********************************************************
 * Heartbeat data
 *
 * @author Mike Cafarella
 ********************************************************/
public class HeartbeatData implements Writable, FSConstants {
    UTF8 name;
    long capacity, remaining;

    /**
     */
    public HeartbeatData() {
        this.name = new UTF8();
    }
        
    /**
     */
    public HeartbeatData(String name, long capacity, long remaining) {
        this.name = new UTF8(name);
        this.capacity = capacity;
        this.remaining = remaining;
    }
        
    /**
     */
    public void write(DataOutput out) throws IOException {
        name.write(out);
        out.writeLong(capacity);
        out.writeLong(remaining);
    }

    /**
     */
    public void readFields(DataInput in) throws IOException {
        this.name = new UTF8();
        name.readFields(in);
        this.capacity = in.readLong();
        this.remaining = in.readLong();
    }

    public UTF8 getName() {
        return name;
    }
    public long getCapacity() {
        return capacity;
    }
    public long getRemaining() {
        return remaining;
    }
}
