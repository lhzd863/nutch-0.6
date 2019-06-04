/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fs;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import net.nutch.io.*;
import net.nutch.ndfs.*;

/****************************************************************
 * Implement the NutchFileSystem interface for the NDFS system.
 *
 * @author Mike Cafarella
 *****************************************************************/
public class NDFSFileSystem extends NutchFileSystem {
    Random r = new Random();

    NDFSClient ndfs;

    /**
     * Create the ShareSet automatically, and then go on to
     * the regular constructor.
     */
    public NDFSFileSystem(InetSocketAddress namenode) throws IOException {
        this.ndfs = new NDFSClient(namenode);
    }

    /**
     * Open the file at f
     */
    public NFSInputStream open(File f) throws IOException {
        return ndfs.open(new UTF8(f.getPath()));
    }

    /**
     * Create the file at f.
     */
    public NFSOutputStream create(File f) throws IOException {
        return create(f, false);
    }
    /**
     */
    public NFSOutputStream create(File f, boolean overwrite) throws IOException {
        return ndfs.create(new UTF8(f.getPath()), overwrite);
    }

    /**
     * Rename files/dirs
     */
    public boolean rename(File src, File dst) throws IOException {
        return ndfs.rename(new UTF8(src.getPath()), new UTF8(dst.getPath()));
    }

    /**
     * Get rid of File f, whether a true file or dir.
     */
    public boolean delete(File f) throws IOException {
        return ndfs.delete(new UTF8(f.getPath()));
    }

    /**
     */
    public boolean exists(File f) throws IOException {
        return ndfs.exists(new UTF8(f.getPath()));
    }

    /**
     */
    public boolean isDirectory(File f) throws IOException {
        return ndfs.isDirectory(new UTF8(f.getPath()));
    }

    /**
     */
    public long getLength(File f) throws IOException {
        NDFSFileInfo info[] = ndfs.listFiles(new UTF8(f.getPath()));
        return info[0].getLen();
    }

    /**
     */
    public File[] listFiles(File f) throws IOException {
        NDFSFileInfo info[] = ndfs.listFiles(new UTF8(f.getPath()));
        File results[] = new NDFSFile[info.length];
        for (int i = 0; i < info.length; i++) {
            results[i] = new NDFSFile(info[i]);
        }
        return results;
    }

    /**
     */
    public void mkdirs(File f) throws IOException {
        ndfs.mkdirs(new UTF8(f.getPath()));
    }

    /**
     * Obtain a filesystem lock at File f.
     */
    public void lock(File f, boolean shared) throws IOException {
        ndfs.lock(new UTF8(f.getPath()), ! shared);
    }

    /**
     * Release a held lock
     */
    public void release(File f) throws IOException {
        ndfs.release(new UTF8(f.getPath()));
    }

    /**
     * Remove the src when finished.
     */
    public void moveFromLocalFile(File src, File dst) throws IOException {
        doFromLocalFile(src, dst, true);
    }

    /**
     * keep the src when finished.
     */
    public void copyFromLocalFile(File src, File dst) throws IOException {
        doFromLocalFile(src, dst, false);
    }

    private void doFromLocalFile(File src, File dst, boolean deleteSource) throws IOException {
        if (exists(dst)) {
            if (! isDirectory(dst)) {
                throw new IOException("Target " + dst + " already exists");
            } else {
                dst = new File(dst, src.getName());
                if (exists(dst)) {
                    throw new IOException("Target " + dst + " already exists");
                }
            }
        }

        if (src.isDirectory()) {
            mkdirs(dst);
            File contents[] = src.listFiles();
            for (int i = 0; i < contents.length; i++) {
                doFromLocalFile(contents[i], new File(dst, contents[i].getName()), deleteSource);
            }
        } else {
            byte buf[] = new byte[4096];
            InputStream in = new BufferedInputStream(new FileInputStream(src));
            try {
                OutputStream out = create(dst);
                try {
                    int bytesRead = in.read(buf);
                    while (bytesRead >= 0) {
                        out.write(buf, 0, bytesRead);
                        bytesRead = in.read(buf);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            } 
        }
        if (deleteSource)
            src.delete();
    }

    /**
     * Takes a hierarchy of files from the NFS system and writes to
     * the given local target.
     */
    public void copyToLocalFile(File src, File dst) throws IOException {
        if (dst.exists()) {
            if (! dst.isDirectory()) {
                throw new IOException("Target " + dst + " already exists");
            } else {
                dst = new File(dst, src.getName());
                if (dst.exists()) {
                    throw new IOException("Target " + dst + " already exists");
                }
            }
        }

        if (isDirectory(src)) {
            dst.mkdirs();
            File contents[] = listFiles(src);
            for (int i = 0; i < contents.length; i++) {
                copyToLocalFile(contents[i], new File(dst, contents[i].getName()));
            }
        } else {
            byte buf[] = new byte[4096];
            InputStream in = open(src);
            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(dst));
                try {
                    int bytesRead = in.read(buf);
                    while (bytesRead >= 0) {
                        out.write(buf, 0, bytesRead);
                        bytesRead = in.read(buf);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            } 
        }
    }

    /**
     * Output will go to the tmp working area.  There may be some source
     * material that we obtain first.
     */
    public File startLocalOutput(File nfsOutputFile, File tmpLocalFile) throws IOException {
        if (exists(nfsOutputFile)) {
            copyToLocalFile(nfsOutputFile, tmpLocalFile);
        }
        return tmpLocalFile;
    }

    /**
     * Move completed local data to NDFS destination
     */
    public void completeLocalOutput(File nfsOutputFile, File tmpLocalFile) throws IOException {
        moveFromLocalFile(tmpLocalFile, nfsOutputFile);
    }

    /**
     * Fetch remote NDFS file, place at tmpLocalFile
     */
    public File startLocalInput(File nfsInputFile, File tmpLocalFile) throws IOException {
        copyToLocalFile(nfsInputFile, tmpLocalFile);
        return tmpLocalFile;
    }

    /**
     * We're done with the local stuff, so delete it
     */
    public void completeLocalInput(File localFile) throws IOException {
        // Get rid of the local copy - we don't need it anymore.
        FileUtil.fullyDelete(localFile);
    }

    /**
     * Create a temp working file, on the remote ndfs disk
     */
    public File createTempFile(String prefix, String suffix, File directory) throws IOException {
        if (directory == null) {
            directory = new File("/tmp");
        }

        while (true) {
            int randomPart = Math.abs(r.nextInt());
            File f = new File(prefix + randomPart + suffix);
            File tmpResult = new File(directory, f.getPath());
            if (! exists(tmpResult) && createNewFile(tmpResult)) {
                return tmpResult;
            }
        }
    }

    /**
     * Shut down the FS.  Not necessary for regular filesystem.
     */
    public void close() throws IOException {
        ndfs.close();
    }

    /**
     */
    public String toString() {
        return "NDFS[" + ndfs + "]";
    }

    /**
     */
    public NDFSClient getClient() {
        return ndfs;
    }
}
