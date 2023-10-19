package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.io.Serializable;
import java.util.HashMap;

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
    public static final File stage = join(GITLET_DIR, "stage");
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File blobs = join(GITLET_DIR, "blobs");

    /** File for repository within .gitlet */
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
        Commit initial = new Commit("initial commit", null);
        File commit_path = join(commits, initial.getId());

        // Create repo with hash for initial commit
        String commit_0_id = initial.getId();
        Repo repo = new Repo();
        repo.master = commit_0_id;
        repo.HEAD = commit_0_id;
        repo.references.put(commit_0_id, initial);

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
     */
    public static void add(String f) {
        // Check if input files exists
        File file = join(CWD, f);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        // Create new blob
        Blob blob = new Blob(file, f);

        // Open current repo
        Repo repo = readObject(repository, Repo.class);

        // Check if blob version exists in previous commit O(1)
        if (repo.stageHas(blob.getId())) {
            // Remove blob from stage
            repo.addTake(blob.getName());
        } else {
            // Add blob to stage (overwrites if same file name)
            repo.addPut(blob.getName(), blob.getId());
        }

        // Save changes
        writeObject(repository, repo);

    }

    /**
     * Creates new commit object with updated content from the staging area,
     * and resets staging area
     */
    public static void commit(String m) {
        // Saves a snapshot of tracked files in the current commit and staging
        // area so they can be restored at a later time, creating a new commit.
        // The commit is said to be tracking the saved files. By default, each
        // commit’s snapshot of files will be exactly the same as its parent
        // commit’s snapshot of files; it will keep versions of files exactly
        // as they are, and not update them. A commit will only update the
        // contents of files it is tracking that have been staged for addition
        // at the time of commit, in which case the commit will now include the
        // version of the file that was staged instead of the version it got from
        // its parent. A commit will save and start tracking any files that were
        // staged for addition but weren’t tracked by its parent. Finally, files
        // tracked in the current commit may be untracked in the new commit as a
        // result being staged for removal by the rm command (below).
        //
        //The bottom line: By default a commit has the same file contents as its
        // parent. Files staged for addition and removal are the updates to the
        // commit. Of course, the date (and likely the mesage) will also different
        // from the parent.
    }
}
