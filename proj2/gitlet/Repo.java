package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class Repo implements Serializable {

    /** Reference to head of the master and side branches */
    public String master; // use this for parent look up
    public String branch;

    /** Reference to current Commit */
    public String HEAD;

    /** Mapping of SHA-1 Strings to all other Objects required for Gitlet */
    public HashMap<String, Commit> commits = new HashMap<>();
    public HashMap<String, Blob> blobs = new HashMap<>();

    /** Name: SHA: Blob adding and removing on stage. */
    public TreeMap<String, HashMap<String, Blob>> add = new TreeMap<>();
    public TreeMap<String, HashMap<String, Blob>> rm = new TreeMap<>();

    /** Tracks SHA-1 Strings of blobs from most recent commit */
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
        add.put(name, tmp);
        blobs.put(sha, blob); // idk how I feel about this
        System.out.println(add.toString());
    }

    public void addTake(String name, String sha) {
        add.remove(name);
        blobs.remove(sha);
    }

    /** Updates rm TreeMap. */
    public void rm(String name, String sha, Blob blob) {
        HashMap<String, Blob> tmp = new HashMap<>();
        tmp.put(sha, blob);
        rm.put(name, tmp);
        blobs.remove(sha, blob);
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
        HashMap<String, Blob> map = add.get(b.name);

        // Check if stage has blob.name
        boolean stageHasName = add.containsKey(b.name);
        if (stageHasName) {
            HashMap<String, Blob> addSha = add.get(b.name);
            boolean stageHasSha = addSha.containsKey(b.id);

            // Check if stage has blob.id
            if (!stageHasSha) {
                Commit parent = commits.get(master); // O(1)
                Blob parentFiles = parent.files.get(b.name); // searching TreeMap takes O(lnN) time

                // Check if parent commit has filename
                boolean isInParentFiles = (parentFiles.name.equals(b.name)); // O(?), probably insignificant
                if (!isInParentFiles) {
                    // Stage blob for addition
                    HashMap<String, Blob> tmp2 = new HashMap<>();
                    tmp2.put(b.id, b);
                    add.put(b.name, tmp2);
                }
            }
        }
        else { // Stage doesn't have name
            // Stage blob for addition
            HashMap<String, Blob> tmp2 = new HashMap<>();
            tmp2.put(b.id, b);
            add.put(b.name, tmp2);
        }
    }
}
