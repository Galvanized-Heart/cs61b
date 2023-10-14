package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class

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
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** Timestamps */
    private String timestamp;

    /** Parent commit */
    private Commit parent;

    /* TODO: fill in the rest of this class. */

    // Constructor
    public Commit(String message, Commit parent) {

    }

    // getMessage()

    // getTimestamp()

    // getParent()
}
