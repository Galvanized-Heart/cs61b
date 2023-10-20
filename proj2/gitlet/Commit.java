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

    /** SHA-1 IDs for this Commit. */
    public String id;

    /** Metadata for Commit. */
    public String message;
    public String timestamp;

    /** SHA-1 ID for each parent Commit. */
    public HashMap<String, Commit> parent;

    /** Name:Blob in Commit. */
    public TreeMap<String, Blob> files; // use blob.id to get sha from existant blobs

    /** Constructor. */
    public Commit(String m, HashMap<String, Commit> p, TreeMap<String, Blob> f) {
        message = m;
        timestamp = new Date().toString();
        parent = p;
        files = f;
        id = sha1(message + parent + timestamp + files); // needs to include blob references too!
        System.out.println("Commit constructed: " + id);
    }
}
