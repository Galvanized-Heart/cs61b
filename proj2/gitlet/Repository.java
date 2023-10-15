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
        stage.mkdir();
        commits.mkdir();
        blobs.mkdir();

        // Create initial commit
        Commit initial = new Commit("initial commit", null);
        File commit_0 = join(commits, initial.getId());

        // Create repo with hash for initial commit
        String commit_0_id = initial.getId();
        Repo repo = new Repo();
        repo.master = commit_0_id;
        repo.HEAD = commit_0_id;
        repo.references.put(commit_0_id, initial);

        // Create persistent files for initial commit and repo
        try {
            commit_0.createNewFile();
            repository.createNewFile();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        // Save initial commit and repo
        Utils.writeObject(commit_0, initial);
        Utils.writeObject(repository, repo);
    }


    /**
     * Adds a blob to the staging area based on the input file
     *
     */
    public static void add(String f) {
        if (!repository.exists()) {
            System.out.println("What did you do to the repository? >:(");
        }
        File file = join(CWD, f);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        // Create blob and add to stage
        // If blob2 has same hash as blob1 already on stage, do not stage it


    }
}
