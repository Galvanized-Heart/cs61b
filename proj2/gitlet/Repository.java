package gitlet;

import java.io.Serializable;
import java.io.File;
import static gitlet.Utils.*;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import java.util.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Maxim Kirby
 */
public class Repository implements Serializable {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, "danger-zone/.gitlet");

    /** Directories within .gitlet. */
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File blobs = join(GITLET_DIR, "blobs");

    /** File for Repo within .gitlet */
    public static final File repository = join(GITLET_DIR, "repository");

    /***************************************************************************************************/

    /** Reference to top of the master and side branches. */
    public HashMap<String, String> branches = new HashMap<>();
    public String currBranch = "";

    /** Reference to current Commit. */
    public String HEAD = null;

    /** Mapping of IDs to all other Objects required for Gitlet. */
    public HashMap<String, Commit> commitSearch = new HashMap<>();
    public HashMap<String, Blob> blobSearch = new HashMap<>();

    /** Name:ID adding and removing on stage. */
    public TreeMap<String, String> add = new TreeMap<>();
    public ArrayList<String> rm = new ArrayList<>();

    /***************************************************************************************************/


    /**
     * Creates directory and files for Gitlet, initializes initial commit,
     * and sets master and HEAD pointers.
     */
    public void initialize() {
        // Create hidden .gitlet directory
        GITLET_DIR.mkdir();

        // Create folders inside .gitlet
        commits.mkdir();
        blobs.mkdir();

        // Create initial commit
        Commit initial = new Commit("initial commit", null, new TreeMap<>());
        File commit_path = join(commits, initial.id);

        // Create repo with hash for initial commit
        String commitID = initial.id;
        branches.put("master", commitID);
        currBranch = "master";
        HEAD = commitID;
        commitSearch.put(commitID, initial);

        // Create persistent files for initial commit and repo
        try {
            commit_path.createNewFile();
            repository.createNewFile();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        // Save initial commit and repo
        writeObject(commit_path, initial);
        writeObject(repository, this);
    }


    /**
     * Adds a blob to the staging area based on the input file
     *
     * IS IT REDUNDANT TO SAVE BLOBS TO REPO AND TO PERSISTENT FILES?
     */
    public void add(String filename) {
        // Check if input files exists
        File file = join(CWD, ("danger-zone/" + filename));
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        // Create new blob
        Blob blob = new Blob(file, filename);

        // Checking components
        Commit currentCommit = commitSearch.get(HEAD);
        String currentFileId = currentCommit.files.get(filename);
        String stagedFileId = add.get(filename);
        String blobID = null;
        boolean overwrite = false;

        // Check if file is in current commit
        if (currentFileId != null) {
            blobID = currentFileId;
        }

        // Check if file is staged for addition
        else if (stagedFileId != null) {
            blobID = stagedFileId;
            overwrite = true;
        }

        // Check if file is a different version of filename
        if (!blob.id.equals(blobID)) {
            // Stage file for addition
            add.put(filename, blob.id);
            blobSearch.put(blob.id, blob);
        }

        // Check if file is same version and is in current commit
        else if (blobID.equals(currentFileId)) {
            // Remove file from addition stage
            add.remove(filename);
        }

        // Delete old blob if overwriting stage
        if (stagedFileId != null) {
            File oldBlobPath = join(blobs, stagedFileId);
            oldBlobPath.delete();
            blobSearch.remove(stagedFileId);
        }

        // Create new blob if it doesn't already exist
        File blobPath = join(blobs, blob.id);
        if (!blobPath.exists()) {
            try {
                blobPath.createNewFile();
                blobSearch.put(blob.id, blob);
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }

            // Save new blob
            writeObject(blobPath, blob);
        }

        // Save changes to repo
        writeObject(repository, this);
    }

    /**
     * Creates new commit object with updated content from the staging area,
     * and resets staging area
     */
    public void commit(String message) {
        // Check if message is empty
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        // Check if stage is empty
        boolean stageIsEmpty = (add.isEmpty() && rm.isEmpty());
        if (stageIsEmpty) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // Fetch current commit and create new commit
        Commit currCommit = commitSearch.get(HEAD);
        TreeMap<String, String> copiedFiles = new TreeMap<>(currCommit.files);
        String branch = branches.get(currBranch);
        Commit newCommit = new Commit(message, branch, copiedFiles);

        // Check if files exists
        if (newCommit.files != null) {
            // Add files that are staged for addition to new commit
            // (overwrites blobs w/ same name in files)
            newCommit.files.putAll(add);

            // Remove files from new commit that are staged for removal
            for (int i = 0; i < rm.size(); i++) {
                newCommit.files.remove(rm.get(i));
            }
            commitSearch.put(newCommit.id, newCommit);
        }

        // Update HEAD and branch pointers
        branches.put(currBranch, newCommit.id);
        HEAD = newCommit.id;
        //debug
        System.out.println("HEAD AND MASTER: "+HEAD + " " + branches.get(currBranch));

        // Clear stage
        add = new TreeMap<>();
        rm = new ArrayList<>();

        // Create persistent files for initial commit and repo
        File commit_path = join(commits, newCommit.id);
        try {
            commit_path.createNewFile();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        // Save initial commit and repo
        writeObject(commit_path, newCommit);
        writeObject(repository, this);
    }

    public void rm(String filename) {
         // Check if file is staged for addition
        if (add.containsKey(filename)) {
            // Remove from staged additions
            add.remove(filename);
        }

        // Check if file is in current commit
        else if (commitSearch.get(HEAD).files.containsKey(filename)) {
            // Stage file for removal
            rm.add(filename);

            // Delete said file if it exists
            File file = join(CWD, ("danger-zone/" + filename));
            if (file.exists()) {
                file.delete();
            }
        }

        else {
            System.out.println("No reason to remove the file.");
        }

        // Save changes to repo
        writeObject(repository, this);
    }

    /**
     * Prints out all commits on the current branch starting from the HEAD pointer.
     * STILL NEED TO DO WORK FOR THE MERGE COMMIT PRINTING!
     */
    public void log() {
        /**
         * (!!! COME BACK TO THIS PART !!!)
         * For merge commits (those that have two parent commits),
         * add a line just below the first that had the first 7 digits
         * of each parents' id.
         *
         * """
         * ===
         * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
         * Merge: 4975af1 2c1ead1
         * Date: Sat Nov 11 12:30:00 2017 -0800
         * Merged development into master.
         *
         * """
         *
         * The first parent is the branch you were on when you did
         * the merge; the second is that of the merged-in branch.
         */

        Commit c = commitSearch.get(HEAD);
        printCommitTree(c);
    }

    // Recursively prints commits+metadata
    private void printCommitTree(Commit c) {
        if (c == null) {
            return;
        }
        printCommit(c);
        printCommitTree(commitSearch.get(c.parent));
    }

    // Prints commit+metadata (make this a method of Commit)
    private void printCommit(Commit c) {
        System.out.println("===");
        System.out.println("commit "+c.id);
        // if (isMerge) { // Figure this out when you understand merge
        // System.out.println("Merge: "+
        //                    c.parent_1.substring(0, 7) +
        //                    " " +
        //                    c.parent_2.substring(0, 7)
        // }
        System.out.println("Date: "+c.timestamp);
        System.out.println(c.message+"\n");
    }

    /** Prints out all commits saved to the .gitlet directory. */
    public void global_log() {
        List<String> commitList = plainFilenamesIn(commits);
        if (commitList != null) {
            for (String str : commitList) {
                Commit c = commitSearch.get(str);
                printCommit(c);
            }
        }
        else {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Prints out all commits save to the .gitlet
     * directory with the specified message.
     */
    public void find(String commitMessage) {
        List<String> commitList = plainFilenamesIn(commits);
        if (commitList != null) {
            for (String str : commitList) {
                Commit c = commitSearch.get(str);
                if (c.message.equals(commitMessage)) {
                    printCommit(c);
                }
            }
        }
        else {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Prints info about current branch, staged, and removed files */
    public void status() {
                // Print out branches
        System.out.println("=== Branches ===");
        for (String name : branches.keySet()) {
            if (name.equals(currBranch)) {
                System.out.print("*");
            }
            System.out.println(name);
        }

        // Print out files staged for addition
        System.out.println("=== Staged Files ===");
        for (String name : add.keySet()) {
            System.out.println(name);
        }
        System.out.println();

        // Print out files staged for removal
        System.out.println("=== Removed Files ===");
        Collections.sort(rm);
        for (String name : rm) {
            System.out.println(name);
        }
        System.out.println();
    }

    /** Checks out files from a designated commit or branch
     *
     * STILL NEED TO SUPPORT CONCATENATED COMMIT IDs
     *
     * UPDATE TO PLAY WELL WITH reset(str)
     *
     * NEED TO UPDATE rm STAGE IF FILE WAS ADDED WHEN IT WAS JUST REMOVED!
     */
    public void checkout(String filename) {
        checkout(filename, HEAD);
    }

    public void checkout(String filename, String commitID) {
        // Fetch file path
        File file = join(CWD, ("danger-zone/" + filename));

        // Check if commit has file
        Commit c = commitSearch.get(commitID);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        // Fetch file version as in commit
        String fileVersion = c.files.get(filename);
        System.out.println(commitID);
        System.out.println(c.files);

        // Check if fileVersion exists in commit
        if (fileVersion == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        // Writes file to CWD if fileVersion exists in commit
        Blob b = blobSearch.get(fileVersion);
        writeContents(file, b.content);
    }

    public void checkoutBranch(String branchName) {
         // remove "danger-zone" features in code after completion (Crtl+F)
        // Check if branch exists
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        // Check if current branch is changing
        if (currBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        // Check if stage contains items
        if (!add.isEmpty() || !rm.isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        // Set file location
        File files = join(CWD, "danger-zone");

        // Fetch files in CWD
        List<String> commitList = plainFilenamesIn(files);
        assert commitList != null;
        Set<String> commitSet = new HashSet<>(commitList); // O(N)

        // Fetch files from target commit
        Commit targCommit = commitSearch.get(branches.get(branchName));
        TreeMap<String, String> targFiles = targCommit.files; // Name:SHA

        // Add files from target commit to CWD
        for (Map.Entry<String, String> entry : targFiles.entrySet()) { // O(N)
            String filename = entry.getKey();
            String fileID = entry.getValue();
            Blob b = blobSearch.get(fileID); // O(1)
            File file_path = join(files, filename);
            writeContents(file_path, b.content);
            commitSet.remove(filename);
        }

        // Remove files in CWD from old commit
        for (String filename : commitSet) { // O(N)
            File file_path = join(files, filename);
            file_path.delete();
        }

        // Check if branch has changed
        if (!currBranch.equals(branchName)) {
            // Clear stage
            add = new TreeMap<>();
            rm = new ArrayList<>();
        }

        // Update HEAD and current-branch pointers
        currBranch = branchName;
        HEAD = branches.get(branchName); // gets SHA of commit at head of branch

        // Save changes to repo
        writeObject(repository, this);
    }

    public void branch(String branchName) {
        // Check if branch with specified name exists
        if (branches.containsKey(branchName)) { // O(1) since hashmap
            System.out.print("A branch with that name already exists.");
            System.exit(0);
        }

        // Copy String from HEAD to new-branch
        branches.put(branchName, HEAD);

        // Save changes to repo
        writeObject(repository, this);
    }

    public void rmBranch(String branchName) {
        // Check if branch exists
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        // Check if currently on specified branch
        if (currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        // Delete branch
        branches.remove(branchName);

        // Save changes to repo
        writeObject(repository, this);
    }


    /** Sets the branch and HEAD pointers to the desired
     *
     * SHOULD I MAKE IT SUCH THAT THE COMMITS BETWEEN END COMMIT AND TARGET COMMIT
     * ARE DELETED FROM .GITLET? THAT WAY IT WON'T SHOW UP ON GLOBAL-LOG
     *
     * Uses elements from checkout
     * Likely need to refactor checkout to play nice with this function
     */
    public void reset(String commitID) { // THIS IS UNTESTED STILL
        // Checkout all files from a target commit

        // Check if commit has file
        Commit c = commitSearch.get(commitID);
        if (c == null) { // CODE FROM checkout(str, str)
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        // Check if stage contains items
        if (!add.isEmpty() || !rm.isEmpty()) { // CODE FROM checkoutBranch(str)
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        // Set file location
        File files = join(CWD, "danger-zone");

        // Fetch files in CWD
        List<String> commitList = plainFilenamesIn(files);
        assert commitList != null;
        Set<String> commitSet = new HashSet<>(commitList); // O(N)

        // Fetch files from target commit
        Commit targCommit = commitSearch.get(commitID);
        TreeMap<String, String> targFiles = targCommit.files; // Name:SHA

        // Add files from target commit to CWD
        for (Map.Entry<String, String> entry : targFiles.entrySet()) { // O(N)
            String filename = entry.getKey();
            String fileID = entry.getValue();
            Blob b = blobSearch.get(fileID); // O(1)
            File file_path = join(files, filename);
            writeContents(file_path, b.content);
            commitSet.remove(filename);
        }

        // Remove files in CWD from old commit
        for (String filename : commitSet) { // O(N)
            File file_path = join(files, filename);
            file_path.delete();
        }

        // Update HEAD and branch pointers
        branches.put(currBranch, commitID);
        HEAD = commitID;

        // Clear stage
        add = new TreeMap<>();
        rm = new ArrayList<>();

        // Save changes to repo
        writeObject(repository, this);

        // Support commitID concatenation (kinda like checkout, fix later)
    }

    /** Merges files from a given branch to the current branch. */
    public void merge(String branchName) {
        // For pseudocode currBranch (HEAD) and branchName!!!

        // Find most recent split point (BFS?)
        Commit split = split(branchName);



        // if split point is == HEAD, Given branch is an ancestor of the current branch. exit



        // If the split point is the currBranch, then the effect is to check out the
        // branchName, and the operation ends after printing the message "Current branch
        // fast-forwarded." exit.
        // Otherwise, we continue with the steps below.


        // 1. File is in split, modded in branchName, not modded in currBranch = add file from branchName
        // 2. File is in split, not modded in branchName, modded in currBranch = add file from currBranch (no staging?)
        // 3. File is in split, modded in branchName, modded in currBranch
            // a. If modded to be same blob = add file from currBranch (no staging?)
            // b. If modded to be diff blob = add merge conflict
        // 4. File not in split, not in branchName, in currBranch = add file from currBranch (no staging?)
        // 5. File not in split, in branchName, not in currBranch = add file from branchName
        // 6. File is in split, not in branchName, not modded in currBranch = remove file
        // 7. File is in split, not modded in branchName, not in currBranch = stays removed (no staging?)


        // in merge conflict:
            // <<<<<<< HEAD
            // <contents of file in currBranch>
            // =======
            // <contents of file in branchName>
            // >>>>>>>
        // (in case of deleted file, leave contents emtpy)
        // and stage file

        // commit stage w/ message "Merged [branchName] into [currBranch]."
        // if merge conflict exists, print "Encountered a merge conflict."
    }

    private Commit split(String branchName) {
        // Perform BFS from each branch pointer and store commitIDs in a set. If a commitID exists in a HashSet, return that commitID


        HashSet<String> set = new HashSet<>();
        ArrayDeque<Commit> queue = new ArrayDeque<>();

        while (!queue.isEmpty()) {

        }

        return new Commit(null, null, null);
    }

    public void test() {
        System.out.println(branches);
        System.out.println(HEAD);
        System.out.println(blobSearch);
    }

}
