/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

import net.nutch.io.*;
import net.nutch.ipc.*;
import net.nutch.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/******************************************************
 * IPC param
 *
 * @author Mike Cafarella
 ******************************************************/
public class FSParam implements Writable, FSConstants {
    public byte op;
    public Writable first;
    public Writable second;

    public FSParam() {
        this((byte) 0, NullWritable.get(), NullWritable.get());
    }

    /**
     */
    public FSParam(byte op) {
        this(op, NullWritable.get(), NullWritable.get());
    }

    /**
     */
    FSParam(byte op, Writable first) {
        this(op, first, NullWritable.get());
    }

    /**
     */
    FSParam(byte op, Writable first, Writable second) {
        this.op = op;
        this.first = first;
        this.second = second;
    }

    /**
     */
    public void write(DataOutput out) throws IOException {
        out.writeByte(op);
        first.write(out);
        second.write(out);
    }

    /**
     * Deserialize the opcode and the args
     */
    public void readFields(DataInput in) throws IOException {
        op = in.readByte();

        switch (op) {
            //
            // Datanode calls
            //
        case OP_HEARTBEAT: {
            first = new HeartbeatData();
            break;
        }
        case OP_BLOCKREPORT: {
            first = new ArrayWritable(Block.class);
            second = new UTF8();
            break;
        }
        case OP_BLOCKRECEIVED: {
            first = new ArrayWritable(Block.class);
            second = new UTF8();
            break;
        }
        case OP_ERROR: {
            first = new UTF8();
            second = new UTF8();
            break;
        }

            //
            // Client calls
            //
        case OP_CLIENT_OPEN: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_STARTFILE: {
            first = new ArrayWritable(UTF8.class);
            second = new BooleanWritable();
            break;
        }
        case OP_CLIENT_ADDBLOCK: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_COMPLETEFILE: {
            first = new ArrayWritable(UTF8.class);
            break;
        }
        case OP_CLIENT_RENAMETO: {
            first = new UTF8();
            second = new UTF8();
            break;
        }
        case OP_CLIENT_DELETE: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_EXISTS: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_ISDIR: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_MKDIRS: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_RENEW_LEASE: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_OBTAINLOCK: {
            first = new ArrayWritable(UTF8.class);
            second = new BooleanWritable();
            break;
        }
        case OP_CLIENT_RELEASELOCK: {
            first = new ArrayWritable(UTF8.class);
            break;
        }
        case OP_CLIENT_LISTING: {
            first = new UTF8();
            break;
        }
        case OP_CLIENT_ABANDONBLOCK: {
            first = new Block();
            second = new UTF8();
            break;
        }
        case OP_CLIENT_RAWSTATS: {
            break;
        }
        case OP_CLIENT_DATANODEREPORT: {
            break;
        }
        default: {
            throw new IOException("Unknown opcode: " + op);
        }
        }

        first.readFields(in);
        second.readFields(in);
    }
}
