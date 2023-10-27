package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.io.Serializable;
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
        repo.master = commit_0_id;
        repo.HEAD = commit_0_id;
        repo.commitSearch.put(commit_0_id, initial); // add to look up

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
        Commit newCommit = new Commit(message, repo.master, currentCommit.files);

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

        // Update HEAD and master pointers
        repo.master = newCommit.id;
        repo.HEAD = newCommit.id;
        System.out.println("HEAD AND MASTER: "+repo.HEAD + " " + repo.master);

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
    }

    public static void log() {
        /**
         * Starting at the current HEAD commit, display information
         * about each commit backwards along the commit tree until
         * the initial commit, following the first parent commit links.
         * (ignore any second parents found in merge commits)
         *
         * java.util.Date and java.util.Formatter are useful
         * for getting and formatting times.
         *
         * """
         * ===
         * commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
         * Date: Thu Nov 9 20:00:05 2017 -0800
         * A commit message.
         *
         * ===
         * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
         * Date: Thu Nov 9 17:01:33 2017 -0800
         * Another commit message.
         *
         * """
         *
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

        // recursion for commits+metadata
        Repo repo = readObject(repository, Repo.class);
        Commit c = repo.commitSearch.get(repo.HEAD);
        printCommitTree(repo, c);
    }

    private static void printCommitTree(Repo repo, Commit c) {

        if (c == null) {
            return;
        }

        printCommit(c);

        printCommitTree(repo, repo.commitSearch.get(c.parent));
    }

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

    public static void global_log() {
        // Get all commits in .gitlet
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
}
