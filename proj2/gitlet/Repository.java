package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import static java.util.Collections.sort;

import java.util.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Maxim Kirby
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** Directories within .gitlet. */
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File blobs = join(GITLET_DIR, "blobs");

    /** File for Repo within .gitlet */
    public static final File repository = join(GITLET_DIR, "repository");

    /***************************************************************************************************/

    /**
     * Creates directory and files for Gitlet, initializes initial commit,
     * and sets master and HEAD pointers.
     */
    public static void initialize() {
        // Create hidden .gitlet directory
        GITLET_DIR.mkdir();

        // Create folders inside .gitlet
        commits.mkdir();
        blobs.mkdir();

        // Create initial commit
        Commit initial = new Commit("initial commit", null, new TreeMap<>());
        File commit_path = join(commits, initial.id);

        // Create repo with hash for initial commit
        String commit_0_id = initial.id;
        Repo repo = new Repo();
        repo.branches.put("master", commit_0_id);
        repo.currBranch = "master";
        repo.HEAD = commit_0_id;
        repo.commitSearch.put(commit_0_id, initial);

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
        writeObject(repository, repo);
    }


    /**
     * Adds a blob to the staging area based on the input file
     *
     * IS IT REDUNDANT TO SAVE BLOBS TO REPO AND TO PERSISTENT FILES?
     */
    public static void add(String filename) {
        // Check if input files exists
        File file = join(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Create new blob
        Blob blob = new Blob(file, filename);

        // Checking components
        Commit currentCommit = repo.commitSearch.get(repo.HEAD);
        String currentFileId = currentCommit.files.get(filename);
        String stagedFileId = repo.add.get(filename);
        String sha = null;
        boolean overwrite = false;

        // Check if file is in current commit
        if (currentFileId != null) {
            sha = currentFileId;
        }

        // Check if file is staged for addition
        else if (stagedFileId != null) {
            sha = stagedFileId;
            overwrite = true;
        }

        // Check if file is a different version of filename
        if (!blob.id.equals(sha)) {
            // Stage file for addition
            repo.add.put(filename, blob.id);
            repo.blobSearch.put(blob.id, blob);
        }

        // Check if file is same version and is in current commit
        else if (sha.equals(currentFileId)) {
            // Remove file from addition stage
            repo.add.remove(filename);
        }

        // Delete old blob if overwriting stage
        if (stagedFileId != null) {
            File oldBlobPath = join(blobs, stagedFileId);
            oldBlobPath.delete();
            repo.blobSearch.remove(stagedFileId);
        }

        // Create new blob if it doesn't already exist
        File blobPath = join(blobs, blob.id);
        if (!blobPath.exists()) {
            try {
                blobPath.createNewFile();
                repo.blobSearch.put(blob.id, blob);
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }

            // Save new blob
            writeObject(blobPath, blob);
        }

        // Save changes to repo
        writeObject(repository, repo);
    }

    /**
     * Creates new commit object with updated content from the staging area,
     * and resets staging area
     */
    public static void commit(String message) {
        // Check if message is empty
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Check if stage is empty
        boolean stageIsEmpty = (repo.add.isEmpty() && repo.rm.isEmpty());
        if (stageIsEmpty) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // Fetch current commit and create new commit
        Commit currentCommit = repo.commitSearch.get(repo.HEAD);
        TreeMap<String, String> copiedFiles = new TreeMap<>(currentCommit.files);
        String currBranch = repo.branches.get(repo.currBranch);
        Commit newCommit = new Commit(message, currBranch, copiedFiles);

        // Check if files exists
        if (newCommit.files != null) {
            // Add files that are staged for addition to new commit
            // (overwrites blobs w/ same name in files)
            newCommit.files.putAll(repo.add);

            // Remove files from new commit that are staged for removal
            for (int i = 0; i < repo.rm.size(); i++) {
                newCommit.files.remove(repo.rm.get(i));
            }
            repo.commitSearch.put(newCommit.id, newCommit);
        }

        // Update HEAD and branch pointers
        repo.branches.put(repo.currBranch, newCommit.id);
        repo.HEAD = newCommit.id;
        //debug
        System.out.println("HEAD AND MASTER: "+repo.HEAD + " " + repo.branches.get(repo.currBranch));

        // Clear stage
        repo.add = new TreeMap<>();
        repo.rm = new ArrayList<>();

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
        writeObject(repository, repo);
    }

    public static void rm(String filename) {
        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Check if file is staged for addition
        if (repo.add.containsKey(filename)) {
            // Remove from staged additions
            repo.add.remove(filename);
        }

        // Check if file is in current commit
        else if (repo.commitSearch.get(repo.HEAD).files.containsKey(filename)) {
            // Stage file for removal
            repo.rm.add(filename);

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
        writeObject(repository, repo);
    }

    /**
     * Prints out all commits on the current branch starting from the HEAD pointer.
     * STILL NEED TO DO WORK FOR THE MERGE COMMIT PRINTING!
     */
    public static void log() {
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

        Repo repo = readObject(repository, Repo.class);
        Commit c = repo.commitSearch.get(repo.HEAD);
        printCommitTree(repo, c);
    }

    // Recursively prints commits+metadata
    private static void printCommitTree(Repo repo, Commit c) {
        if (c == null) {
            return;
        }
        printCommit(c);
        printCommitTree(repo, repo.commitSearch.get(c.parent));
    }

    // Prints commit+metadata (make method of Commit)
    private static void printCommit(Commit c) {
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
    public static void global_log() {
        List<String> commitList = plainFilenamesIn(commits);
        Repo repo = readObject(repository, Repo.class);
        if (commitList != null) {
            for (String str : commitList) {
                Commit c = repo.commitSearch.get(str);
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
    public static void find(String commitMessage) {
        List<String> commitList = plainFilenamesIn(commits);
        Repo repo = readObject(repository, Repo.class);
        if (commitList != null) {
            for (String str : commitList) {
                Commit c = repo.commitSearch.get(str);
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
    public static void status() {
        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Print out branches
        System.out.println("=== Branches ===");
        for (String name : repo.branches.keySet()) {
            if (name.equals(repo.currBranch)) {
                System.out.print("*");
            }
            System.out.println(name);
        }

        // Print out files staged for addition
        System.out.println("=== Staged Files ===");
        for (String name : repo.add.keySet()) {
            System.out.println(name);
        }
        System.out.println();

        // Print out files staged for removal
        System.out.println("=== Removed Files ===");
        Collections.sort(repo.rm);
        for (String name : repo.rm) {
            System.out.println(name);
        }
        System.out.println();
    }

    /** Checks out files from a designated commit
     *
     * STILL NEED TO SUPPORT CONCATENATED COMMIT IDs
     * STILL NEED TO SUPPORT BRANCH CHANGES
     */
    public static void checkout(String filename) {
        Repo repo = readObject(repository, Repo.class);
        checkout(filename, repo.HEAD);
    }

    public static void checkout(String filename, String sha) {
        // Fetch file path
        File file = join(CWD, filename);

        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Check if commit has file
        Commit c = repo.commitSearch.get(sha);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        // Fetch file version as in commit
        String fileVersion = c.files.get(filename);
        System.out.println(sha);
        System.out.println(c.files);

        // Check if fileVersion exists in commit
        if (fileVersion == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        // Writes file to CWD if fileVersion exists in commit
        Blob b = repo.blobSearch.get(fileVersion);
        writeContents(file, b.content);
    }

    public static void checkoutBranch(String branch) {
        // Take files from head of commit of a branch
        // Puts commit version of files in CWD overwriting current version of files (if they exist)
        // Any files from current branch that are not in checked out branch are to be deleted
        // If (checked-out branch != current branch) clear stage
        // HEAD will be set to current branch

        // Fetch files from either master or other-branch
        // For each file in most front commit in that branch,
            // write over fileVersion based on commit
        // Delete any files from the current branch that are not in checked out branch
            // Does this mean I have to compare the files in each branch?
        // Clear the stage if branches were swapped and do not clear if you are in the same branch
        // Update HEAD to current branch
    }

    public static void branch(String branchName) {
        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Check if branch with specified name exists
        if (repo.branches.containsKey(branchName)) { // O(1) since hashmap
            System.out.print("A branch with that name already exists.");
            System.exit(0);
        }

        // Copy String from HEAD to new-branch
        repo.branches.put(branchName, repo.HEAD);
    }

    public static void test() {
        Repo repo = readObject(repository, Repo.class);
    }

}
