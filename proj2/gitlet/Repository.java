package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.io.Serializable;
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
     * */
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
     * gonna have to check the previous commit for new stage
     *
     * Bugs:
     * 1. If an addition is made, a file of the same name but different contents creates
     *    a new persistent blob.
     * 2.
     */
    public static void add(String filename) {
        // Check if input files exists
        File file = join(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        // Create new blob
        Blob blob = new Blob(file, filename);

        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Attempt to add to stage
        repo.addToStage(blob);

        // Create persistent files for initial commit and repo
        File blob_path = join(blobs, blob.id);
        try {
            blob_path.createNewFile();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        // Save changes
        writeObject(blob_path, blob);
        writeObject(repository, repo);


    }

    /**
     * Creates new commit object with updated content from the staging area,
     * and resets staging area
     */
    public static void commit(String m) {
        // Check if message is empty
        if (m.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Check if stage is empty
        boolean stageIsEmpty = (repo.add.isEmpty() && repo.rm.isEmpty());
        if (stageIsEmpty) {
            System.out.println(repo.add.toString());
            System.out.println(repo.blobSearch.toString());

            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // Create commit with blobs that commit should be tracking from parent commit (UID)
        //curCommit.files.add(repo.blobs);

        // Update commit with staged items (UID)


        // The bottom line: By default a commit has the same file contents as its
        // parent. Files staged for addition and removal are the updates to the
        // commit. Of course, the date (and likely the message) will also different
        // from the parent.

        // Commit will have same files as parents by default.
        // Commit will only update contents of files it is tracking that have been staged for addition.
        // Commit will save/track files that were staged for addition but weren't tracked by parent.
        // New commit may untrack files from prev commit that were stages for removal.

        // Stage is cleared after commit is created.
        // Move master and HEAD pointers to this newly made commit.

        // Don’t store redundant copies of versions of files that a commit receives from its parent
        // (hint: remember that blobs are content addressable and use the SHA1 to your advantage).

        /** PUT THIS IN COMMIT SECTION TO UPDATE VALUE FIELD OF COMMIT WITH EXISTANT FILENAME
         boolean isSameVersion = inquiry.id.equals(blob.id); // O(?), probably insignificant
         if (!isSameVersion) {

         }
         */

        // Put in parent sha from master
        Commit parCommit = repo.commitSearch.get(repo.master);
        System.out.println(parCommit);

        // Copy files from parent commit to current commit
        Commit curCommit = new Commit(m, repo.master, parCommit.files);

        // Add files from stage to current commit (overwrite blobs w/ same name in files)
        for (var entry : repo.add.entrySet()) { // O(N)
            curCommit.files.put(entry.getKey(), entry.getValue());
        }
        System.out.println(curCommit.files);


        //curCommit.files.putAll(repo.add); // have to make sure curCommit.files is not null to use
        //curCommit.files.put(repo.;)

        // Remove files from current commit (remove blobs w/ same name)
        for (var entry : repo.rm.entrySet()) { // O(N)
            curCommit.files.remove(entry.getKey());
        }
        System.out.println(curCommit.files);

        // Update master ptr
        repo.master = curCommit.id;



        // Clear stage
        repo.add = new TreeMap<>();

        // Update prevCommit Set?


        // Create persistent files for initial commit and repo
        File commit_path = join(commits, curCommit.id);
        try {
            commit_path.createNewFile();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        // Save initial commit and repo
        writeObject(commit_path, curCommit);
        writeObject(repository, repo);

    }

    public static void rm(String f) {
        // unstage file if it is staged for addition
        // if file is in current commit, stage for removal and remove from CWD

        // rm needs to search for blob by file name
            // cannot make a new blob and compare it
            // could try accessing repo.master (to get current commit)
            //
    }

    public static void test() {
        File f = join(CWD, "code.txt");
        System.out.println();
    }
}
