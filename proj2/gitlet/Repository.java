package gitlet;

import java.io.Serializable;
import java.io.File;
import static gitlet.Utils.*;

import java.nio.charset.StandardCharsets;
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

    /***************************************************************************************************
    INSTANCE VARIABLES */

    /** The maximum length of SHA hash. */
    public static final int MAX_ID_LEN = 40;

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, "danger-zone/.gitlet");

    /** Directories within .gitlet. */
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File blobs = join(GITLET_DIR, "blobs");

    /** File for Repo within .gitlet */
    public static final File repository = join(GITLET_DIR, "repository");

    /** Reference to top of the master and side branches. */
    public HashMap<String, String> branches = new HashMap<>();
    public String currBranch = null;

    /** Reference to current Commit. */
    public String HEAD = null;

    /** Mapping of IDs to all other Objects required for Gitlet. */
    public HashMap<String, Commit> commitSearch = new HashMap<>();
    public HashMap<String, Blob> blobSearch = new HashMap<>();

    /** Name:ID adding and removing on stage. */
    public TreeMap<String, String> add = new TreeMap<>();
    public ArrayList<String> rm = new ArrayList<>();

    /***************************************************************************************************
    MAIN METHODS */

    /** Creates directory and files for Gitlet, initializes initial commit,
     * and sets master and HEAD pointers. */
    public void initialize() {
        // Create hidden .gitlet directory
        GITLET_DIR.mkdir();

        // Create folders inside .gitlet
        commits.mkdir();
        blobs.mkdir();

        // Create initial commit
        Commit initial = new Commit("initial commit", null, new TreeMap<>());
        File commitPath = join(commits, initial.id);

        // Create repo with hash for initial commit
        String commitID = initial.id;
        branches.put("master", commitID);
        currBranch = "master";
        HEAD = commitID;
        commitSearch.put(commitID, initial);

        // Create persistent files for initial commit and repo
        try {
            commitPath.createNewFile();
            repository.createNewFile();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        // Save initial commit and repo
        writeObject(commitPath, initial);
        writeObject(repository, this);
    }

    /** Sets blob to be added to next commit by staging it for addition. */
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

    /** Prints out all commits in the current branch starting from the HEAD pointer
     * all the way to the initial commit. */
    public void log() {
        Commit c = commitSearch.get(HEAD);
        printCommitTree(c);
    }

    /** Prints out all commits saved to the .gitlet directory. */
    public void global_log() {
        List<String> commitList = plainFilenamesIn(commits);
        if (commitList == null) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        for (String str : commitList) {
            Commit c = commitSearch.get(str);
            System.out.println(c.toString());
        }
    }

    /** Prints out all commits saved to the .gitlet directory with
     * the specified message. */
    public void find(String commitMessage) {
        List<String> commitList = plainFilenamesIn(commits);
        if (commitList == null) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        for (String str : commitList) {
            Commit c = commitSearch.get(str);
            if (c.message.equals(commitMessage)) {
                System.out.println(c.toString());
            }
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
        writeObject(repository, this);
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
        writeObject(repository, this);
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
        writeObject(repository, this);
    }

    /** Checks out all the files for a specified commit.
     *
     * SHOULD I MAKE IT SUCH THAT THE COMMITS BETWEEN END COMMIT AND TARGET COMMIT
     * ARE DELETED FROM .GITLET? THAT WAY IT WON'T SHOW UP ON GLOBAL-LOG
     */
    public void reset(String commitID) {
        // Check if concatenated ID
        if (commitID.length() < MAX_ID_LEN) {
            commitID = findCommit(commitID);
        }

        // Changes files in CWD to files in commit
        Commit commit = commitSearch.get(commitID);
        checkoutFiles(commit);

        // Update HEAD and branch pointers
        branches.put(currBranch, commitID);
        HEAD = commitID;

        // Clear stage
        add = new TreeMap<>();
        rm = new ArrayList<>();

        // Save changes to repo
        writeObject(repository, this);
    }

    /** Creates a new commit that merges files from a given branch to the current branch. */
    public void merge(String branchName) {
        // Find split point
        String other = branches.get(branchName);
        Commit split = splitFind(other, HEAD);

        // Check if split and branch end are the same commit
        if (HEAD.equals(split.id)) {
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        // Check if split and HEAD are the same commit
        if (other.equals(split.id)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        // Fetch commits for each branch
        Commit thisBranch = commitSearch.get(HEAD);
        Commit thatBranch = commitSearch.get(other);

        // Find all unique filenames
        Set<String> filenames = getAllFilenames(split.files, thisBranch.files, thatBranch.files);

        System.out.println(filenames);

        // Iterate over all filenames and update commit accordingly
            for (String filename : filenames) {
                boolean isInSplit = split.files.containsKey(filename);
                boolean isInThis = thisBranch.files.containsKey(filename);
                boolean isInThat = thatBranch.files.containsKey(filename);
                boolean thisIsMod = false;
                boolean thatIsMod = false;

            // Check if file from thisBranch is modified compared to split point
            if (isInThis && isInSplit) {
                // True if files are different versions
                thisIsMod = !thisBranch.files.get(filename).equals(split.files.get(filename));
            } else if (!isInThis && isInSplit) {
                // True if file was deleted since split
                thisIsMod = true;
            }

            // Check if file from thatBranch is modified compared to split point
            if (isInThat && isInSplit) {
                // True if files are different versions
                thatIsMod = !thatBranch.files.get(filename).equals(split.files.get(filename));
            } else if (!isInThat && isInSplit) {
                // True if file was deleted since split
                thatIsMod = true;
            }

            // Check if file needs to be updated and update accordingly
            if (thisIsMod && thatIsMod) {
                // Merge conflict for 2 files
                String thisFile = thisBranch.files.get(filename);
                String thatFile = thatBranch.files.get(filename);
                mergeConflict(blobSearch.get(thisFile), blobSearch.get(thatFile));
                add(filename);
            } else if (isInSplit && isInThis && !isInThat) {
                rm(filename);
            } else if ((!isInSplit && !isInThis && isInThat) || (isInSplit && !thisIsMod && thatIsMod)) {
                File dz = join(CWD, "danger-zone");
                File filePath = join(dz, filename);
                String file = thatBranch.files.get(filename);
                System.out.println(file);
                Blob blob = blobSearch.get(file);
                writeContents(filePath, blob.content);
                add(filename);
            }
        }
        Commit c = commit("Merged " + branchName + " into " + currBranch + ".");
        c.parents[1] = other;

        // Save changes to repo
        writeObject(repository, this);

        // 3. File is in split, modded in thatBranch, modded in thisBranch
            // b. If modded to be diff blob or deleted = add merge conflict

        // 1. File is in split, modded in thatBranch, in thisBranch = add file from thatBranch
        // 5. File not in split, in thatBranch, not in thisBranch = add file from thatBranch

        // 6. File is in split, not in thatBranch, in thisBranch = remove file

        // 7. File is in split, in thatBranch, not in thisBranch = stays removed (no staging?)
        // 2. File is in split, in thatBranch, modded in thisBranch = add file from thisBranch (no staging?)
        // 4. File not in split, not in thatBranch, in thisBranch = add file from thisBranch (no staging?)
        // 3. File is in split, modded in thatBranch, modded in thisBranch
            // a. If modded to be same blob = add file from thisBranch (no staging?)
    }

    /***************************************************************************************************
     HELPER METHODS */

    /** Recursively prints commits + metadata. */
    private void printCommitTree(Commit c) {
        if (c == null) {
            return;
        }
        System.out.println(c.toString());
        printCommitTree(commitSearch.get(c.parents[0]));
    }

    /** Checks out files for a given commit or
     * a single file for if filename is given. */
    private void checkoutFiles(Commit commit) {
        checkoutFiles(commit, null);
    }
    private void checkoutFiles(Commit commit, String filename) {
        // Check if commit exists
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        // Set file location
        File filesPath = join(CWD, "danger-zone");

        // Check if filename is specified
        if (filename != null) {
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
            writeContents(repository, this);
        }

        // If filename is null, checkout all the files
        else {
            // Check if stage contains items
            if (!add.isEmpty() || !rm.isEmpty()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            // Fetch files in CWD
            List<String> commitList = plainFilenamesIn(filesPath);
            assert commitList != null;
            Set<String> filesRemaining = new HashSet<>(commitList);

            // Fetch files from new commit
            TreeMap<String, String> files = commit.files; // Name:SHA

            // Add files from new commit to CWD
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String fileName = entry.getKey();
                String fileID = entry.getValue();

                // Update file contents
                Blob b = blobSearch.get(fileID);
                File file_path = join(filesPath, fileName);
                writeContents(file_path, b.content);

                // Update remaining files
                filesRemaining.remove(filename);
            }

            // Remove remaining files in CWD from old commit
            for (String fileName : filesRemaining) {
                File file_path = join(filesPath, fileName);
                file_path.delete();
            }
        }

        // Save changes to repo
        writeObject(repository, this);
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
    private Commit splitFind(String branch1, String branch2) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        // Add branch commitIDs to queue
        queue.offer(branch1);
        queue.offer(branch2);

        // Loop until common ancestor is found or queue is empty
        while (!queue.isEmpty()) {
            String currentCommit = queue.poll();

            // Check if common ancestor
            if (visited.contains(currentCommit)) {
                return commitSearch.get(currentCommit);
            }

            // Mark commitID as visited
            visited.add(currentCommit);

            // Add parent commits to queue
            Commit current = commitSearch.get(currentCommit);
            for (String parent : current.parents) {
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
        File dz = join(CWD, "danger-zone");
        File filePath = null;

        // Read contents from blob1 as string
        if (blob1 != null) {
            filePath = join(dz, blob1.name);
            content1 = new String(blob1.content, StandardCharsets.UTF_8);
        } else { content1 = ""; }

        // Read contents from blob2 as string
        if (blob2 != null) {
            filePath = join(dz, blob2.name);
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

    public void test() {
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
}
