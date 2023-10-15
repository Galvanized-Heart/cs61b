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

    public static final File stage = join(GITLET_DIR, "stage");
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File blobs = join(GITLET_DIR, "blobs");
    public static final File repository = join(GITLET_DIR, "repository");


    /***************************************************************************************************/


    /** Returns Repository object if a Repository exists or one is being created */
    public static Repo findRepo(boolean repoExists, boolean isInit) {
        Repo repo = null;
        if (repoExists && !isInit) {
            File repo_path = Utils.join(Repository.GITLET_DIR, "repository");
            if (repo_path.exists()) {
                repo = Utils.readObject(repo_path, Repo.class);
                System.out.println("Found repo!");
            } else {
                System.out.println("Could not find Gitlet repository.");
                System.out.println("Try 'java gitlet.Main reset' and then 'java gitlet.Main init' to fix this issue.");
                System.exit(0);
            }
        } else if (!repoExists && !isInit) {
            repo = new Repo();
            System.out.println("New repo created!");
        } else if (repoExists && isInit) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        return repo;
    }


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
        Commit initial = new Commit("initial commit", null, null);
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
        if (!GITLET_DIR.exists()) {

        }
        File file = join(CWD, f);
        if (!file.exists()) {
            System.out.println("Unable to retrieve file name: " + f);
            return;
        }

    }
}
