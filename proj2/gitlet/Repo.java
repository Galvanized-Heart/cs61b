package gitlet;

import edu.princeton.cs.algs4.ST;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class Repo implements Serializable {

    /**
     * Reference to top of the master and side branches
     */
    public HashMap<String, String> branches = new HashMap<>();
    public String currBranch;

    /**
     * Reference to current Commit
     */
    public String HEAD;

    /**
     * Mapping of SHA-1 Strings to all other Objects required for Gitlet
     */
    public HashMap<String, Commit> commitSearch = new HashMap<>();
    public HashMap<String, Blob> blobSearch = new HashMap<>();

    /**
     * Name:SHA adding and removing on stage.
     */
    public TreeMap<String, String> add = new TreeMap<>();
    public ArrayList<String> rm = new ArrayList<>();

    /***************************************************************************************************/

    // Search for commit (get/put)
    // Search for blob (get/put)
    // Search for branch (get/put)
    // Edit add (get?/put/remove)
    // Edit rm
    // Edit HEAD

}
