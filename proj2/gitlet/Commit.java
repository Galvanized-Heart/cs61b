package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Maxim Kirby
 */

public class Commit implements Serializable {
    // TODO: Make instance variables private and create methods for access

    /** ID for this Commit. */
    public String id;

    /** Metadata for Commit. */
    public String message;
    public String timestamp;

    /** IDs for each parent Commit. */
    public String[] parents = new String[2];


    /** Name:BlobID for files in Commit. */
    public TreeMap<String, String> files;

    /** Constructor. */
    public Commit(String m, String p, TreeMap<String, String> f) {
        message = m;
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        timestamp = sdf.format(currentDate);
        parents[0] = p;
        files = f;
        id = sha1(message+parents[0]+timestamp+files);
    }

    /** Formats printing of Commit. */
    @Override
    public String toString() {
        String result = "===\n" + "commit " + id;
        if (parents[1] != null) {
            result += "\nMerge: "+ parents[0].substring(0, 7) + " " + parents[1].substring(0, 7);
        }
        result += "\nDate: " + timestamp + "\n" + message+"\n";
        return result;
    }

    /** Sets secondary parent to specified CommitID */
    public void setOtherParent(String commitID) {
        parents[1] = commitID;
    }
}
