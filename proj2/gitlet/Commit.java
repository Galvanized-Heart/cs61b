package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeSet;

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

    /** SHA-1 IDs for this Commit */
    private String id;

    /** Metadata for Commit */
    private String message;
    private String timestamp;

    /** SHA-1 ID for each parent Commit */
    private String parent;

    /** List of SHA-1 ID for each file version Blob in Commit */
    private TreeSet<String> files;
    // this might have to be made into a Tree for lg(N) runtime.
    // not sure because commit says size of .gitlet matters.

    /**

    /** Constructor */
    public Commit(String m, String p) {
        message = m;
        timestamp = new Date().toString();
        parent = p;

        id = Utils.sha1(message+parent+timestamp);
        System.out.println("Commit constructed: "+ id);
    }

    /** Returns String of Commit's SHA-1 ID */
    public String getId() {
        return id;
    }

    /** Returns String of Commit's message */
    public String getMessage() {
        return message;
    }

    /** Returns String of Commit's timestamp */
    public String getTimestamp() {
        return timestamp;
    }

    /** Returns String of Commit parent's SHA-1 ID */
    public String getParent() {
        return parent;
    }

    /** Returns HashMap of Strings of Commit blob(s)'s SHA-1 ID */
    public TreeSet<String> getFiles() {
        return files;
    }

    /** Adds Blob SHA-1 ID to Commit files. */
    public void addBlobToFiles(String sha) {
        files.add(sha);
    }

    /** Removes Blob SHA-1 ID to Commit files. */
    public void removeBlobFromFiles(String sha) {
        files.remove(sha);
    }
}
