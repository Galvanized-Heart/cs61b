package gitlet;

import edu.neu.ccs.gui.BooleanView;

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

    /** Reference to head of the master and side branches */
    public static String master; // use this for parent look up
    public static String branch; // use this for branch loop up

    /** Reference to current Commit */
    public static String HEAD; // use this for checked out commit

    /** Mapping of SHA-1 Strings to all other Objects required for Gitlet */
    public static HashMap<String, Commit> commitConvert = new HashMap<>(); // Store all SHA:Commits
    public static HashMap<String, Blob> blobsConvert = new HashMap<>(); // Store all SHA:Blobs

    /** Name: SHA: Blob adding and removing on stage. */
    public static TreeMap<String, String> add = new TreeMap<>(); // Store Name:SHA
    public static TreeMap<String, String> rm = new TreeMap<>(); // Store Name:SHA

    /** Tracks SHA-1 Strings of blobs from most recent commit */
    public static HashSet<String> prevCommit = new HashSet<>();

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
        Commit initial = new Commit("initial commit", null, null);
        File commit_path = join(commits, initial.id);

        // Create repo with hash for initial commit
        String commit_0_id = initial.id;
        Repository repo = new Repository();
        master = commit_0_id;
        HEAD = commit_0_id;
        commitConvert.put(commit_0_id, initial);

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

        // Check if stage has blob.name
        boolean stageHasName = repo.add.containsKey(filename);
        if (stageHasName) {
            HashMap<String, Blob> addSha = repo.add.get(filename);
            boolean stageHasSha = addSha.containsKey(blob.id);

            // Check if stage has blob.id
            if (!stageHasSha) {
                Commit parent = repo.commits.get(repo.master); // O(1)
                Blob parentFiles = parent.files.get(filename); // searching TreeMap takes O(lnN) time

                // Check if parent commit has filename
                boolean isInParentFiles = (parentFiles.name.equals(filename)); // O(?), probably insignificant
                if (!isInParentFiles) {
                    // Stage blob for addition
                    HashMap<String, Blob> tmp2 = new HashMap<>();
                    tmp2.put(blob.id, blob);
                    repo.add.put(filename, tmp2);
                }
            }
        }
        else { // Stage doesn't have name
            // Stage blob for addition
            HashMap<String, Blob> tmp2 = new HashMap<>();
            tmp2.put(blob.id, blob);
            repo.add.put(filename, tmp2);
        }


        /** PUT THIS IN COMMIT SECTION TO UPDATE VALUE FIELD OF COMMIT WITH EXISTANT FILENAME
        boolean isSameVersion = inquiry.id.equals(blob.id); // O(?), probably insignificant
        if (!isSameVersion) {

        }
         */

        // Check if blob version exists in previous commit O(1)
        if (repo.stageHas(blob.getId())) {
            // Remove blob from stage
            repo.addTake(blob.getName(), blob.getId());
        } else {
            // Add blob to stage (overwrites if same file name)
            repo.addPut(blob.getName(), blob.getId(), blob);
        }

        // Save changes
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
        if (repo.stageIsEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // Create commit with blobs that commit should be tracking from parent commit (UID)
        System.out.println(repo.master);
        Commit parCommit = repo.commits.get(repo.master);
        System.out.println(parCommit.files);
        //Commit curCommit = new Commit(m, repo.master, parCommit.files);
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

        // Put in parent sha from master
        // Copy files from parent commit to current commit
        // Add files from stage to current commit (overwrite blobs w/ same name in files)
        // Remove files from current commit (remove blobs w/ same name)

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
