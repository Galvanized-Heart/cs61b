package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class Repo implements Serializable {

    /** Reference to head of the master and side branches */
    public String master;
    public String branch;

    /** Reference to current Commit */
    public String HEAD;

    /** Mapping of SHA-1 Strings to all other Objects required for Gitlet */
    public HashMap<String, Object> references = new HashMap<>();

    /** Mappings for SHA-1 Strings by File names for adding and removing on stage. */
    private TreeMap<String,String> add = new TreeMap<>();
    private TreeMap<String,String> rm = new TreeMap<>();

    /** Tracks SHA-1 Strings of blobs from most recent commit */
    private HashSet<String> prevCommit = new HashSet<>();

    /***************************************************************************************************/

    /** Returns serialized object based on SHA-1 ID. */
    public Object sha1ToObj(String sha, Object objType) {
        return references.get(sha);
    }

    /** Updates references HashMap for new Objects values using SHA-1 keys. */
    public void addRef(String sha, Object obj) {
        references.put(sha, obj);
    }

    /** Updates add TreeMap. */
    public void addPut(String name, String sha) {
        add.put(name, sha);
        System.out.println(add.toString());
    }

    public void addTake(String name) {
        add.remove(name);
    }

    /** Updates rm TreeMap. */
    public void rm(String name, String sha) {
        rm.put(name, sha);
        System.out.println(rm.toString());
    }


    public boolean stageHas(String sha) {
        return prevCommit.contains(sha);
    }

}
