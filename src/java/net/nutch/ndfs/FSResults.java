/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

import net.nutch.io.*;
import net.nutch.ipc.*;
import net.nutch.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*****************************************************
 * The result of an NFS IPC call.
 * 
 * @author Mike Cafarella
 *****************************************************/
public class FSResults implements Writable, FSConstants {
    public byte op;
    public Writable first;
    public Writable second;
    boolean success = false;
    boolean tryagain = false;

    /**
     */
    public FSResults() {
        this((byte) 0, NullWritable.get(), NullWritable.get());
    }
    public FSResults(byte op) {
        this(op, NullWritable.get(), NullWritable.get());
    }
    public FSResults(byte op, Writable first) {
        this(op, first, NullWritable.get());
    }

    /**
     */
    public FSResults(byte op, Writable first, Writable second) {
        this.op = op;
        this.first = first;
        this.second = second;
    }

    /**
     * Whether the call worked.
     */
    public boolean success() {
        return success;
    }

    /**
     * Whether the client should give it another shot
     */
    public boolean tryagain() {
        return tryagain;
    }

    /**
     */
    public void write(DataOutput out) throws IOException {
        out.writeByte(op);
        first.write(out);
        second.write(out);
    }

    /**
     */
    public void readFields(DataInput in) throws IOException {
        op = in.readByte();

        switch (op) {
            //
            // Issued to DataNodes
            //
        case OP_INVALIDATE_BLOCKS: {
            success = true;
            first = new ArrayWritable(Block.class);
            break;
        }
        case OP_TRANSFERBLOCKS: {
            success = true;
            first = new ArrayWritable(Block.class);
            second = new TwoDArrayWritable(DatanodeInfo.class);
            break;
        }
        case OP_ACK: {
            success = true;
            break;
        }

            //
            // Client operations
            //
        case OP_CLIENT_OPEN_ACK: {
            success = true;
            first = new ArrayWritable(Block.class);
            second = new TwoDArrayWritable(DatanodeInfo.class);
            break;
        }
        case OP_CLIENT_STARTFILE_ACK:
        case OP_CLIENT_ADDBLOCK_ACK: {
            success = true;
            first = new Block();
            second = new ArrayWritable(DatanodeInfo.class);
            break;
        }
        case OP_CLIENT_COMPLETEFILE_ACK: {
            success = true;
            break;
        }
        case OP_CLIENT_RENAMETO_ACK:
        case OP_CLIENT_DELETE_ACK: {
            success = true;
            break;
        }
        case OP_CLIENT_LISTING_ACK: {
            success = true;
            first = new ArrayWritable(NDFSFileInfo.class);
            break;
        }
        case OP_CLIENT_OBTAINLOCK_ACK: {
            success = true;
            break;
        }
        case OP_CLIENT_RELEASELOCK_ACK: {
            success = true;
            break;
        }
        case OP_CLIENT_EXISTS_ACK:
        case OP_CLIENT_ISDIR_ACK:
        case OP_CLIENT_MKDIRS_ACK: {
            success = true;
            break;
        }
        case OP_CLIENT_RENEW_LEASE_ACK: {
            success = true;
            break;
        }
        case OP_CLIENT_ABANDONBLOCK_ACK: {
            success = true;
            break;
        }
        case OP_CLIENT_RAWSTATS_ACK: {
            success = true;
            first = new ArrayWritable(LongWritable.class);
            break;
        }
        case OP_CLIENT_DATANODEREPORT_ACK: {
            success = true;
            first = new ArrayWritable(DatanodeInfo.class);
            break;
        }
        case OP_CLIENT_TRYAGAIN: {
            success = true;
            tryagain = true;
            break;
        }
        case OP_FAILURE: {
            success = false;
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

