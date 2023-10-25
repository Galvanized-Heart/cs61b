package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

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
     * IT WOULD BE IDEAL IF WHEN YOU UPDATED A FILE IN THE ADD
     * STAGE IT DELETES THE OLD PERSISTENT FILE!!!
     *
     * BLOBSEARCH() NEEDS TO BE UPDATED SOMEWHERE!!!
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

        // Check if file is in current commit
        if (currentFileId != null) {
            sha = currentFileId;
        }

        // Check if file is staged for addition
        else if (stagedFileId != null) {
            sha = stagedFileId;
            // Trigger for deleting old blob?
        }

        // Check if file is a different version of filename
        if (!blob.id.equals(sha)) {
            // Stage file for addition
            repo.add.put(filename, blob.id);

            // Add to blobSearch? Where else?
        }
        else {
            // Remove file from addition stage
            repo.add.remove(filename);
        }

        // Create new blob if it doesn't already exist
        File blob_path = join(blobs, blob.id);
        if (!blob_path.exists()) {
            try {
                blob_path.createNewFile();
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }

            // Save new blob
            writeObject(blob_path, blob);
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
        }

        // Update master pointer
        repo.master = newCommit.id;

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
                restrictedDelete(file);
            }
        }

        else {
            System.out.println("No reason to remove the file.");
        }
    }

    public static void log() {
        return;
    }
}
