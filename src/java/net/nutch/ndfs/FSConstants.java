/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.ndfs;

/************************************
 * Some handy constants
 *
 * @author Mike Cafarella
 ************************************/
public interface FSConstants {
    public static int BLOCK_SIZE = 32 * 1000 * 1000;
    //public static int BLOCK_SIZE = 2173;

    public static final long WRITE_COMPLETE = 0xcafae11a;

    //
    // IPC Opcodes 
    //
    // Processed at namenode
    public static final byte OP_ERROR = (byte) 0;
    public static final byte OP_HEARTBEAT = (byte) 1;
    public static final byte OP_BLOCKRECEIVED = (byte) 2;
    public static final byte OP_BLOCKREPORT = (byte) 3;
    public static final byte OP_TRANSFERDATA = (byte) 4;

    // Processed at namenode, from client
    public static final byte OP_CLIENT_OPEN = (byte) 20;
    public static final byte OP_CLIENT_STARTFILE = (byte) 21;
    public static final byte OP_CLIENT_ADDBLOCK = (byte) 22;
    public static final byte OP_CLIENT_RENAMETO = (byte) 23;
    public static final byte OP_CLIENT_DELETE = (byte) 24;  
    public static final byte OP_CLIENT_COMPLETEFILE = (byte) 25;
    public static final byte OP_CLIENT_LISTING = (byte) 26;
    public static final byte OP_CLIENT_OBTAINLOCK = (byte) 27;
    public static final byte OP_CLIENT_RELEASELOCK = (byte) 28;
    public static final byte OP_CLIENT_EXISTS = (byte) 29;
    public static final byte OP_CLIENT_ISDIR = (byte) 30;
    public static final byte OP_CLIENT_MKDIRS = (byte) 31;
    public static final byte OP_CLIENT_RENEW_LEASE = (byte) 32;
    public static final byte OP_CLIENT_ABANDONBLOCK = (byte) 33;
    public static final byte OP_CLIENT_RAWSTATS = (byte) 34;
    public static final byte OP_CLIENT_DATANODEREPORT = (byte) 35;
    
    // Processed at datanode, back from namenode
    public static final byte OP_ACK = (byte) 40;
    public static final byte OP_TRANSFERBLOCKS = (byte) 41;    
    public static final byte OP_INVALIDATE_BLOCKS = (byte) 42;
    public static final byte OP_FAILURE = (byte) 43;

    // Processed at client, back from namenode
    public static final byte OP_CLIENT_OPEN_ACK = (byte) 60;
    public static final byte OP_CLIENT_STARTFILE_ACK = (byte) 61;
    public static final byte OP_CLIENT_ADDBLOCK_ACK = (byte) 62;
    public static final byte OP_CLIENT_RENAMETO_ACK = (byte) 63;
    public static final byte OP_CLIENT_DELETE_ACK = (byte) 64;
    public static final byte OP_CLIENT_COMPLETEFILE_ACK = (byte) 65;
    public static final byte OP_CLIENT_TRYAGAIN = (byte) 66;
    public static final byte OP_CLIENT_LISTING_ACK = (byte) 67;
    public static final byte OP_CLIENT_OBTAINLOCK_ACK = (byte) 68;
    public static final byte OP_CLIENT_RELEASELOCK_ACK = (byte) 69;
    public static final byte OP_CLIENT_EXISTS_ACK = (byte) 70;  
    public static final byte OP_CLIENT_ISDIR_ACK = (byte) 71;
    public static final byte OP_CLIENT_MKDIRS_ACK = (byte) 72;
    public static final byte OP_CLIENT_RENEW_LEASE_ACK = (byte) 73;    
    public static final byte OP_CLIENT_ABANDONBLOCK_ACK = (byte) 74;
    public static final byte OP_CLIENT_RAWSTATS_ACK = (byte) 75;
    public static final byte OP_CLIENT_DATANODEREPORT_ACK = (byte) 76;

    // Processed at datanode stream-handler
    public static final byte OP_WRITE_BLOCK = (byte) 80;
    public static final byte OP_READ_BLOCK = (byte) 81;
    public static final byte OP_READSKIP_BLOCK = (byte) 82;

    // Encoding types
    public static final byte RUNLENGTH_ENCODING = 0;
    public static final byte CHUNKED_ENCODING = 1;

    // Return codes for file create
    public static final int OPERATION_FAILED = 0;
    public static final int STILL_WAITING = 1;
    public static final int COMPLETE_SUCCESS = 2;

    //
    // Timeouts, constants
    //
    public static long HEARTBEAT_INTERVAL = 3 * 1000;
    public static long EXPIRE_INTERVAL = 4 * HEARTBEAT_INTERVAL;
    public static long BLOCKREPORT_INTERVAL = 9 * 60 * 1000;
    public static long OBSOLETE_INTERVAL = 10 * 60 * 1000;
    public static long SYSTEM_STARTUP_PERIOD = 15 * 1000;
    public static long DATANODE_STARTUP_PERIOD = 30 * 1000;
    public static long LEASE_PERIOD = 16 * 1000;
}
