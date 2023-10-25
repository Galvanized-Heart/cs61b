package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class Repo implements Serializable {

    /**
     * Reference to head of the master and side branches
     */
    public String master; // use this for parent look up
    public String branch; // use this for branch loop up

    /**
     * Reference to current Commit
     */
    public String HEAD; // use this for checked out commit

    /**
     * Mapping of SHA-1 Strings to all other Objects required for Gitlet
     */
    public HashMap<String, Commit> commitSearch = new HashMap<>(); // Store all SHA:Commits
    public HashMap<String, Blob> blobSearch = new HashMap<>(); // Store all SHA:Blobs

    /**
     * Name:SHA adding and removing on stage.
     */
    public TreeMap<String, String> add = new TreeMap<>(); // Store Name:SHA
    public ArrayList<String> rm = new ArrayList<>(); // Store Name

    /**
     * Tracks SHA-1 Strings of blobs from most recent commit (might be deprecated)
     */
    public HashSet<String> prevCommit = new HashSet<>();

    /***************************************************************************************************/

    /** Returns serialized object based on SHA-1 ID. */
    public Object sha1ToObj(String sha, Object objType) {
        return null;
        //references.get(sha);
    }

    /** Updates references HashMap for new Objects values using SHA-1 keys. */
    public void addRef(String sha, Object obj) {
        //references.put(sha, obj);
    }

    /** Updates add TreeMap. */
    public void addPut(String name, String sha, Blob blob) {
        HashMap<String, Blob> tmp = new HashMap<>();
        tmp.put(sha, blob);
        //add.put(name, tmp);
        blobSearch.put(sha, blob); // idk how I feel about this
        System.out.println(add.toString());
    }

    public void addTake(String name, String sha) {
        add.remove(name);
        blobSearch.remove(sha);
    }

    /** Updates rm TreeMap. */
    public void rm(String name, String sha, Blob blob) {
        HashMap<String, Blob> tmp = new HashMap<>();
        tmp.put(sha, blob);
        //rm.put(name, tmp);
        blobSearch.remove(sha, blob);
        System.out.println(rm.toString());
    }


    public boolean stageHas(String sha) {
        return prevCommit.contains(sha);
    }

    public boolean stageIsEmpty() {
        return (add.isEmpty() && rm.isEmpty());
    }

    /** Attempts to add blob to staging area */
    public void addToStage(Blob b) {
        /** vvv THIS SECTION COULD BE REFACTORED! vvv */
        // Check if stage has blob.name
        boolean stageHasName = add.containsKey(b.name);
        if (stageHasName) {

            // Check if stage has blob.id
            boolean stageHasSha = add.containsKey(b.name); // O(lgN)
            if (!stageHasSha) {
                Commit parent = commitSearch.get(master); // O(1)
                Blob parentFiles = blobSearch.get(parent.files.get(b.name)); // searching TreeMap takes O(lgN) time

                // Check if parent commit has filename
                boolean isInParentFiles = (parentFiles.name.equals(b.name)); // O(?), probably insignificant
                if (!isInParentFiles) {
                    // Stage blob for addition
                    add.put(b.name, b.id);
                    blobSearch.put(b.name, b);
                }
            }
        } else {
            // Stage blob for addition
            add.put(b.name, b.id);
            blobSearch.put(b.name, b);
        }
        /** ^^^ THIS SECTION COULD BE REFACTORED! ^^^ */
    }
}
