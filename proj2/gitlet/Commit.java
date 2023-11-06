package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Date;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Maxim Kirby
 */

/**
 *                  !!! Don't use pointers for objects !!!
 *
 * Instead, you must create a hashMap that will link SHA1 keys to Object values
 * and this can then be serialized.
 *
 * Additionally, objects that use pointers for references will instead store the
 * SHA1 strings so that they can be serialized and the hashMap will allow for
 * referencing a parent or blob when necessary!
 *
 * Transient fields do not get serialized and when they are deserialized, they
 * are fit with their default values (e.g. null for pointers).
 *                  "private transient MyCommitType parent1;"
 * Using transient fields for pointers would allow you to not require lookup the
 * hashMap every time you want to reference another object.
 * This can be done by setting the transient field when a lookup is used or something.
 * */



public class Commit implements Serializable {

    /** SHAs for this Commit. */
    public String id;

    /** Metadata for Commit. */
    public String message;
    public String timestamp;

    /** IDs for each parent Commit. */
    public String[] parents = new String[2];


    /** Name:SHA in Commit. */
    public TreeMap<String, String> files;

    /** Constructor. */
    public Commit(String m, String p, TreeMap<String, String> f) {
        message = m;
        timestamp = new Date().toString();
        parents[0] = p;
        files = f;
        id = sha1(message + parents[0] + timestamp + files);
        //System.out.println("Commit constructed: " + message);
    }

    @Override
    public String toString() {
        System.out.println("===");
        System.out.println("commit "+id);
        // if (isMerge) { // Figure this out when you understand merge
        // System.out.println("Merge: "+
        //                    c.parent_1.substring(0, 7) +
        //                    " " +
        //                    c.parent_2.substring(0, 7)
        // }
        System.out.println("Date: "+timestamp);
        System.out.println(message+"\n");
        return "";
    }

    public void setOtherParent(String op) {
        parents[1] = op;
    }
}
