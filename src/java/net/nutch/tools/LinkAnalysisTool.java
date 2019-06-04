/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.tools;

import java.io.*;
import java.util.*;

import net.nutch.fs.*;
import net.nutch.util.*;

/***************************************
 * LinkAnalysisTool performs link-analysis by using the
 * DistributedAnalysisTool.  This single-process all-in-one
 * tool is a wrapper around the more complicated distributed
 * one.  
 *
 * @author Mike Cafarella
 ***************************************/
public class LinkAnalysisTool {   
    NutchFileSystem nfs;
    File dbDir;
    DistributedAnalysisTool dat = null;

    /**
     * We need a DistributedAnalysisTool in order to get
     * things done!
     */
    public LinkAnalysisTool(NutchFileSystem nfs, File dbDir) throws IOException {
        this.nfs = nfs;
        this.dbDir = dbDir;
        this.dat = new DistributedAnalysisTool(nfs, dbDir);
    }

    /**
     * Do a single-process iteration over the database.  Implemented
     * by calling the distributed tool's functions.
     */
    public void iterate(int numIterations, File scoreFile) throws IOException {
        for (int i = 0; i < numIterations; i++) {
            File tmpDir = nfs.createTempFile("tmpdir", "la", dbDir);
            nfs.delete(tmpDir);
            nfs.mkdirs(tmpDir);

            dat.initRound(1, tmpDir);
            dat.computeRound(0, tmpDir);
            dat.completeRound(tmpDir, scoreFile);
        }
    }

    /**
     * Kick off the link analysis.  Submit the location of the db
     * directory, as well as the cache size.
     */
    public static void main(String argv[]) throws IOException {
        if (argv.length < 2) {
            System.out.println("usage: java net.nutch.tools.LinkAnalysisTool (-local | -ndfs <namenode:port>) <db_dir> <numIterations>");
            return;
        }

        NutchFileSystem nfs = NutchFileSystem.parseArgs(argv, 0);
        File dbDir = new File(argv[0]);
        int numIterations = Integer.parseInt(argv[1]);

        System.out.println("Started at " + new Date(System.currentTimeMillis()));
        try {
            LinkAnalysisTool lat = new LinkAnalysisTool(nfs, dbDir);
            lat.iterate(numIterations, new File(dbDir, "linkstats.txt"));
        } finally {
            nfs.close();
            System.out.println("Finished at " + new Date(System.currentTimeMillis()));
        }
    }
}
