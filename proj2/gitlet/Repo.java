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

    /***************************************************************************************************/


}
