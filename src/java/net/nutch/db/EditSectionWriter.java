/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.db;

import java.io.*;
import java.util.*;

import net.nutch.io.*;
import net.nutch.fs.*;
import net.nutch.util.*;

/**********************************************
 * EditSectionWriter writes a discrete portion of a WebDB.
 * The WebDBWriter class may instantiate many EditSectionWriter
 * objects to do its work (and always instantiates at 
 * least one).
 *
 * @author Mike Cafarella
 ***********************************************/
public class EditSectionWriter {
    private final int COMPLETION_VERSION = 0;
    public final static String WRITE_METAINFO_PREFIX = "metainfo.";
    public final static String EDITS_PREFIX = "edits.";

    static int numSections;

    /**
     * Remove all the edits in this shareGroup from the indicated emitter.
     */
    /**
    public static void removeEdits(NutchFile curSection, int emitter) throws IOException {
        NutchFile edits = new NutchFile(curSection, EDITS_PREFIX + emitter);
        NutchFile writeMetaInfo = new NutchFile(curSection, WRITE_METAINFO_PREFIX + emitter);
        NutchFile writeCompletion = new NutchFile(curSection, WRITE_COMPLETION_PREFIX + emitter);        

        NutchFileSystem nfs = edits.getFS();
        nfs.delete(edits);
        nfs.delete(writeMetaInfo);
        nfs.delete(writeCompletion);
    }
    **/

    /**
     * Remove all the edits in this section, from all emitters.
     */
    /**
    public static void removeAllEdits(NutchFile curSection) throws IOException {
        for (int i = 0; i < numSections; i++) {
            removeEdits(curSection, i);
        }
    }
    **/

    
    File editsDir, editsList;
    File editsListFile;
    int numEdits = 0;
    boolean closed = false;
    NutchFileSystem nfs;
    SequenceFile.Writer seqWriter;

    /**
     * Make a EditSectionWriter for the appropriate file.
     */
    public EditSectionWriter(NutchFileSystem nfs, String label, int targetNum, int writerNum, Class keyClass, Class valClass) throws IOException {
        this.nfs = nfs;

        File allEditsDir = new File("editsection." + targetNum, "editsdir." + writerNum);
        this.editsDir = new File(allEditsDir, label);
        this.editsList = new File(editsDir, "editslist");
        this.seqWriter = new SequenceFile.Writer(nfs, editsList.getPath(), keyClass, valClass);
    }

    /**
     * Add a key/val pair
     */
    public synchronized void append(WritableComparable key, Writable val) throws IOException {
        if (closed) {
            throw new IOException("EditSectionWriter is closed");
        }
        seqWriter.append(key, val);
        numEdits++;
    }

    /**
     * Close down the EditSectionWriter.  Afterwards, write down
     * the completion file (including the number of edits inside).
     */
    public synchronized void close() throws IOException {
        if (closed) {
            throw new IOException("EditSectionWriter is already closed");
        }

        // Close down sequence writer
        seqWriter.close();

        // Separately write down the number of edits
        File editsInfo = new File(editsDir, "editsinfo");
        DataOutputStream out = new DataOutputStream(nfs.create(editsInfo));
        try {
            out.write(COMPLETION_VERSION);
            out.writeInt(numEdits);
        } finally {
            out.close();
        }
    }
}
