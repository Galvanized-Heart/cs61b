package gitlet;

import java.io.Serializable;
import java.io.File;
import static gitlet.Utils.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Maxim Kirby
 */
public class Repository implements Serializable {

    /***************************************************************************************************
     INSTANCE VARIABLES */

    /** The maximum length of SHA hash. */
    public static final int MAX_ID_LEN = 40;

    /** The current working directory. */
    // public static final File CWD = join(new File(System.getProperty("user.dir")), "danger-zone"); // Remove join and "danger-zone" when done testing
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** Directories within .gitlet. */
    public static final File COMMITS = join(GITLET_DIR, "commits");
    public static final File BLOBS = join(GITLET_DIR, "blobs");

    /** File for Repo within .gitlet */
    public static final File REPOSITORY = join(GITLET_DIR, "repository");

    /** Reference to top of the master and side branches. */
    private HashMap<String, String> branches = new HashMap<>();
    private String currBranch = null;

    /** Reference to current Commit. */
    private String HEAD = null;

    /** Mapping of IDs to all other Objects required for Gitlet. */
    private HashMap<String, Commit> commitSearch = new HashMap<>();
    private HashMap<String, Blob> blobSearch = new HashMap<>();

    /** Name:ID adding and removing on stage. */
    private TreeMap<String, String> add = new TreeMap<>();
    private ArrayList<String> rm = new ArrayList<>();

    /***************************************************************************************************
     MAIN METHODS */

    /** Creates directory and files for Gitlet, initializes initial commit,
     * and sets master and HEAD pointers. */
    public void initialize() {
        // Create hidden .gitlet directory
        GITLET_DIR.mkdir();

        // Create folders inside .gitlet
        COMMITS.mkdir();
        BLOBS.mkdir();

        // Create initial commit
        Commit commit = new Commit("initial commit", null, new TreeMap<>());
        File commitPath = join(COMMITS, commit.id);

        // Create repo with hash for initial commit
        String commitID = commit.id;
        branches.put("master", commitID);
        currBranch = "master";
        HEAD = commitID;
        commitSearch.put(commitID, commit);

        // Create persistent files for initial commit and repo
        try {
            commitPath.createNewFile();
            REPOSITORY.createNewFile();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Save initial commit and repo
        writeObject(commitPath, commit);
        writeObject(REPOSITORY, this);
    }

    /** Sets blob to be added to next commit by staging it for addition. */
    public void add(String filename) {
        // Check if input files exists
        File filePath = join(CWD, filename);
        if (!filePath.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        // Create new blob
        Blob blob = new Blob(filePath, filename);

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
            add.remove(filename);
        }

        // Delete old blob if overwriting stage
        if (stagedFileId != null) {
            File oldBlobPath = join(BLOBS, stagedFileId);
            oldBlobPath.delete();
            blobSearch.remove(stagedFileId);
        }

        // Create new blob if it doesn't already exist
        File blobPath = join(BLOBS, blob.id);
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

        // TODO: Add functionality to take out a file from the removal stage if it is there and
        //  do not add it to add stage

        // Save changes to repo
        writeObject(REPOSITORY, this);
    }

    /** Creates new commit object with updated content from the staging area,
     * and resets staging area. */
    public Commit commit(String message) {
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

        // Clear stage
        add = new TreeMap<>();
        rm = new ArrayList<>();

        // Create persistent files for initial commit and repo
        File commitPath = join(COMMITS, newCommit.id);
        try {
            commitPath.createNewFile();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Save initial commit and repo
        writeObject(commitPath, newCommit);
        writeObject(REPOSITORY, this);

        return newCommit;
    }

    /** Sets blob to be removed from next commit by staging it for removal
     * and removes file current working directory. */
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
            File file = join(CWD, filename);
            if (file.exists()) {
                file.delete();
            }
        }

        else {
            System.out.println("No reason to remove the file.");
        }

        // Save changes to repo
        writeObject(REPOSITORY, this);
    }

    /** Prints out all commits in the current branch starting from the HEAD pointer
     * all the way to the initial commit. */
    public void log() {
        Commit commit = commitSearch.get(HEAD);
        printCommitTree(commit);
    }

    /** Prints out all commits saved to the .gitlet directory. */
    public void globalLog() {
        List<String> commitList = plainFilenamesIn(COMMITS);
        for (String commitID : commitList) {
            Commit commit = commitSearch.get(commitID);
            System.out.println(commit.toString());
        }
    }

    /** Prints out all commits saved to the .gitlet directory with
     * the specified message. */
    public void find(String commitMessage) {
        boolean cannotFindMessage = true;
        List<String> commitList = plainFilenamesIn(COMMITS);
        for (String commitID : commitList) {
            Commit commit = commitSearch.get(commitID);
            if (commit.message.equals(commitMessage)) {
                System.out.println(commit.id); // check this change in gradescope!
                cannotFindMessage = false;
            }
        }
        if (cannotFindMessage) {
            System.out.println("Found no commit with that message."); // check this change in gradescope!
            System.exit(0);
        }
    }

    /** Prints info about current branch, staged, and removed files */
    public void status() {
        // Print out branches
        System.out.println("=== Branches ===");
        for (String branch : branches.keySet()) {
            if (branch.equals(currBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();

        // Print out files staged for addition
        System.out.println("=== Staged Files ===");
        for (String file : add.keySet()) {
            System.out.println(file);
        }
        System.out.println();

        // Print out files staged for removal
        System.out.println("=== Removed Files ===");
        Collections.sort(rm);
        for (String file : rm) {
            System.out.println(file);
        }
        System.out.println();

        // Print out unstaged files that have been modified
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        // Print out untracked files
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Checks out files from a branch of a single file from a designated commit. */
    public void checkout(String filename) {
        checkout(filename, HEAD);
    }
    public void checkout(String filename, String commitID) {
        // Check if concatenated ID
        if (commitID.length() < MAX_ID_LEN) {
            commitID = findCommit(commitID);
        }

        // Changes file to version in commit
        Commit commit = commitSearch.get(commitID);
        checkoutFiles(commit, filename);
    }

    public void checkoutBranch(String branchName) {
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

        // Changes files in CWD to files in branch commit
        Commit commit = commitSearch.get(branches.get(branchName));
        checkoutFiles(commit);

        // Check if branch has changed
        if (!currBranch.equals(branchName)) {
            // Clear stage
            add = new TreeMap<>();
            rm = new ArrayList<>();
        }

        // Update HEAD and current-branch pointers
        currBranch = branchName;
        HEAD = branches.get(branchName);

        // Save changes to repo
        writeObject(REPOSITORY, this);
    }

    /** Adds a new branch to the map of branches. */
    public void branch(String branchName) {
        // Check if branch with specified name exists
        if (branches.containsKey(branchName)) {
            System.out.print("A branch with that name already exists.");
            System.exit(0);
        }

        // Copy String from HEAD to new-branch
        branches.put(branchName, HEAD);

        // Save changes to repo
        writeObject(REPOSITORY, this);
    }

    /** Removes an existing branch from the map of branches. */
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
        writeObject(REPOSITORY, this);
    }

    /** Checks out all the files for a specified commit. */
    public void reset(String commitID) {
        // Check if concatenated ID
        if (commitID.length() < MAX_ID_LEN) {
            commitID = findCommit(commitID);
        }

        if (!commitSearch.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        // Changes files in CWD to files in commit
        Commit commit = commitSearch.get(commitID);
        checkoutFiles(commit); // TODO: Make is such that errors are caught outside this function

        // Update HEAD and branch pointers
        branches.put(currBranch, commitID);
        HEAD = commitID;

        // Clear stage
        add = new TreeMap<>();
        rm = new ArrayList<>();

        // Save changes to repo
        writeObject(REPOSITORY, this);
    }

    /** Creates a new commit that merges files from a given branch to the current branch. */
    public void merge(String branchName) {
        // TODO: Check for untracked file that would be deleted from CWD
        if (false) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        // Check if there are staged items
        if (!add.isEmpty() || !rm.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        // Check if branch exists
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        // Check if given branch is the current branch
        if (branchName.equals(currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        // Find split point
        String thisID = HEAD;
        String thatID = branches.get(branchName);
        Commit split = splitFind(thatID, thisID);

        // Check if split and branch end are the same commit
        if (thisID.equals(split.id)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        // Check if split and HEAD are the same commit
        if (thatID.equals(split.id)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        // Fetch commits for each branch
        Commit thisCommit = commitSearch.get(thisID);
        Commit thatCommit = commitSearch.get(thatID);

        // Find all unique filenames
        Set<String> filenames = getAllFilenames(split.files, thisCommit.files, thatCommit.files);

        // Iterate over all filenames and update commit accordingly
        for (String filename : filenames) {
            // Check components
            boolean isInSplit = split.files.containsKey(filename);
            boolean isInThis = thisCommit.files.containsKey(filename);
            boolean isInThat = thatCommit.files.containsKey(filename);
            boolean thisIsMod = false;
            boolean thatIsMod = false;

            // Check if file from thisBranch is modified compared to split point
            if (isInThis && isInSplit) {
                // True if files are different versions
                thisIsMod = !thisCommit.files.get(filename).equals(split.files.get(filename));
            } else if (!isInThis && isInSplit) {
                // True if file was deleted since split
                thisIsMod = true;
            }

            // Check if file from thatBranch is modified compared to split point
            if (isInThat && isInSplit) {
                // True if files are different versions
                thatIsMod = !thatCommit.files.get(filename).equals(split.files.get(filename));
            } else if (!isInThat && isInSplit) {
                // True if file was deleted since split
                thatIsMod = true;
            }

            // Check if file needs to be updated and update accordingly
            if (thisIsMod && thatIsMod) {
                // Merge conflict for 2 files
                String thisFile = thisCommit.files.get(filename);
                String thatFile = thatCommit.files.get(filename);
                mergeConflict(blobSearch.get(thisFile), blobSearch.get(thatFile));
                add(filename);
            } else if (isInSplit && isInThis && !isInThat) {
                rm(filename);
            } else if ((!isInSplit && !isInThis && isInThat) || (isInSplit && !thisIsMod && thatIsMod)) {
                File filePath = join(CWD, filename);
                String file = thatCommit.files.get(filename);
                Blob blob = blobSearch.get(file);
                writeContents(filePath, blob.content);
                add(filename);
            }
        }

        // Create merge commit and update 2nd parent
        Commit commit = commit("Merged " + branchName + " into " + currBranch + ".");
        commit.parents[1] = thatID;

        // Save changes to repo
        writeObject(REPOSITORY, this);
    }

    /***************************************************************************************************
     HELPER METHODS */

    /** Recursively prints commits + metadata. */
    private void printCommitTree(Commit commit) {
        if (commit == null) {
            return;
        }
        System.out.println(commit.toString());
        printCommitTree(commitSearch.get(commit.parents[0]));
    }

    /** Checks out files for a given commit or
     * a single file for if filename is given. */
    private void checkoutFiles(Commit commit) {
        checkoutFiles(commit, null);
    }
    private void checkoutFiles(Commit commit, String filename) {
        // TODO: Remove error catches from this function!
        // Check if commit exists
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        // Check if filename is specified
        if (filename != null) {
            // Set file location
            File filesPath = join(CWD, filename);

            // Fetch file version as in commit
            String fileVersion = commit.files.get(filename);

            // Check if fileVersion exists in commit
            if (fileVersion == null) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }

            // Writes file to CWD if fileVersion exists in commit
            Blob b = blobSearch.get(fileVersion);
            writeContents(filesPath, b.content);

            // Updates stage
            rm.remove(filename);
            writeObject(REPOSITORY, this);
        }

        // If filename is null, checkout all the files
        else {
            // Check if stage contains items
            if (!add.isEmpty() || !rm.isEmpty()) { // this check should actually be different
                // TODO: Make it so that we have a way to track untracked files and update
                //  this section!
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            // Fetch files in CWD
            List<String> commitList = plainFilenamesIn(CWD);
            assert commitList != null;
            Set<String> filesRemaining = new HashSet<>(commitList);

            // Fetch files from new commit
            TreeMap<String, String> commitFiles = commit.files;

            // Add files from new commit to CWD
            for (Map.Entry<String, String> entry : commitFiles.entrySet()) {
                String fileName = entry.getKey();
                String fileID = entry.getValue();

                // Update file contents
                Blob b = blobSearch.get(fileID);
                File filePath = join(CWD, fileName);
                writeContents(filePath, b.content);

                // Update remaining files
                filesRemaining.remove(fileName);
            }

            // Remove remaining files in CWD from old commit
            for (String fileName : filesRemaining) {
                File filePath = join(CWD, fileName);
                filePath.delete();
            }
        }

        // Save changes to repo
        writeObject(REPOSITORY, this);
    }

    /** Returns the full length ID from a partial ID of a commit. */
    private String findCommit(String shortID) {
        for (String commitID : commitSearch.keySet()) {
            if (commitID.startsWith(shortID)) {
                return commitID;
            }
        }
        return shortID;
    }

    /** Traverses branches until the most recent common ancestor Commit
     * is found using BFS. */
    private Commit splitFind(String branchID1, String branchID2) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        // Add branch commitIDs to queue
        queue.offer(branchID1);
        queue.offer(branchID2);

        // Loop until common ancestor is found or queue is empty
        while (!queue.isEmpty()) {
            String currCommitID = queue.poll();

            // Check if common ancestor
            if (visited.contains(currCommitID)) {
                return commitSearch.get(currCommitID);
            }

            // Mark commitID as visited
            visited.add(currCommitID);

            // Add parent commits to queue
            Commit currCommit = commitSearch.get(currCommitID);
            for (String parent : currCommit.parents) {
                if (parent != null) {
                    queue.offer(parent);
                }
            }
        }

        // No common ancestor found
        return null;
    }

    /** Returns a set containing all the unique filenames from 3 TreeMaps. */
    private Set<String> getAllFilenames(TreeMap<String, String> map1, TreeMap<String, String> map2, TreeMap<String, String> map3) {
        Set<String> uniqueFileNames = new HashSet<>(map1.keySet());
        uniqueFileNames.addAll(map2.keySet());
        uniqueFileNames.addAll(map3.keySet());
        return uniqueFileNames;
    }

    /** Rewrites a merge conflicted file to contain contents from both
     * versions of the file. */
    private void mergeConflict(Blob blob1, Blob blob2) {
        System.out.println("Encountered a merge conflict.");

        // Initialize contents and filepath
        String content1;
        String content2;
        File filePath = null;

        // Read contents from blob1 as string
        if (blob1 != null) {
            filePath = join(CWD, blob1.name);
            content1 = new String(blob1.content, StandardCharsets.UTF_8);
        } else { content1 = ""; }

        // Read contents from blob2 as string
        if (blob2 != null) {
            filePath = join(CWD, blob2.name);
            content2 = new String(blob2.content, StandardCharsets.UTF_8);
        } else { content2 = ""; }

        // Build merge conflict contents
        String result = "<<<<<<< HEAD\n" + content1 + "\n" + "=======\n" + content2 + "\n" + ">>>>>>>";
        byte[] bytes = result.getBytes();

        // Update file with merge conflict contents
        writeContents(filePath, bytes);
    }

    /***************************************************************************************************
     TEST METHODS */

    public void testMerging() {
        File a = join(GITLET_DIR, "../a.txt");
        writeContents(a, "aaa");
        File b = join(GITLET_DIR, "../b.txt");
        writeContents(b, "bbb");
        File c = join(GITLET_DIR, "../c.txt");
        writeContents(c, "ccc");
        File d = join(GITLET_DIR, "../d.txt");
        writeContents(d, "ddd");
        File e = join(GITLET_DIR, "../e.txt");
        writeContents(e, "eee");
        File f = join(GITLET_DIR, "../f.txt");
        writeContents(f, "fff");
        File h = join(GITLET_DIR, "../h.txt");
        writeContents(h, "hhh");
        File i = join(GITLET_DIR, "../i.txt");
        writeContents(i, "iii");

        // Build split commit
        add("a.txt");
        add("b.txt");
        add("c.txt");
        add("d.txt");
        add("e.txt");
        add("h.txt");
        add("i.txt");
        commit("This will be the split node");

        // Create other branch
        branch("other-branch");

        // Build 1st commit away from split in master branch
        writeContents(a, "aaa*");
        add("a.txt");
        writeContents(b, "bbb*");
        add("b.txt");
        commit("Commit 1 in master branch");

        // Build 2nd commit away from split in master branch
        rm("e.txt");
        add("f.txt");
        writeContents(h, "hhh*");
        add("h.txt");
        rm("i.txt");
        commit("Commit 2 in master branch");

        // Switch to other branch
        checkoutBranch("other-branch");

        // Build 1st commit away from split in other branch
        writeContents(a, "aaa**");
        add("a.txt");
        writeContents(c, "ccc*");
        add("c.txt");
        rm("d.txt");
        File g = join(GITLET_DIR, "../g.txt");
        writeContents(g, "ggg");
        add("g.txt");
        rm("h.txt");
        writeContents(i, "iii*");
        add("i.txt");
        commit("Commit 1 in other branch");

        checkoutBranch("master");

        System.out.println("\n========\n");

        merge("other-branch");

        System.out.println("\n========\n");

        System.out.println(commitSearch.get(HEAD));
    }

    public void testBasicCheckout() {
        File wug = join(GITLET_DIR, "../wug.txt");
        writeContents(wug, "wug.txt");

        add("wug.txt");

        commit("added wug");

        writeContents(wug, "notwug.txt");

        checkout("wug.txt");
    }

    // CHANGE CWD BEFORE SUBMITTING TO GRADESCOPE!!!

    public void test33() { // Im pretty sure this works as intended. Status prints branches in wrong order?
        File f = join(GITLET_DIR, "../f.txt");
        writeContents(f, "wug.txt");
        File g = join(GITLET_DIR, "../g.txt");
        writeContents(g, "notwug.txt");
        add("f.txt");
        add("g.txt");
        commit("Two files");

        branch("other");

        File h = join(GITLET_DIR, "../h.txt");
        writeContents(h, "wug2.txt");
        add("h.txt");
        rm("g.txt");
        commit("Add h.txt and remove g.txt");

        checkoutBranch("other");

        rm("f.txt");
        File k = join(GITLET_DIR, "../k.txt");
        writeContents(k, "wug3.txt");
        add("k.txt");
        commit("Add k.txt and remove f.txt");

        checkoutBranch("master");

        merge("other");
    }

    public void test34() {
        // Test says it failed on the merge conflict for f.txt (my inspection of files indicates it works as intended)
        File f = join(GITLET_DIR, "../f.txt");
        writeContents(f, "wug.txt");
        File g = join(GITLET_DIR, "../g.txt");
        writeContents(g, "notwug.txt");
        add("f.txt");
        add("g.txt");
        commit("Two files");

        branch("other");

        File h = join(GITLET_DIR, "../h.txt");
        writeContents(h, "wug2.txt");
        add("h.txt");
        rm("g.txt");
        writeContents(f, "wug2.txt");
        add("f.txt");
        commit("Add h.txt, remove g.txt, and change f.txt");

        checkoutBranch("other");

        writeContents(f, "notwug.txt");
        add("f.txt");
        File k = join(GITLET_DIR, "../k.txt");
        writeContents(k, "wug3.txt");
        add("k.txt");
        commit("Add k.txt and modify f.txt");

        checkoutBranch("master");

        merge("other");
    }

    public void test36() {

    }
}
