package gitlet;

import java.io.File;

import static gitlet.Utils.*;
// maybe java.nio.file.Files
// maybe java.io and java.nio have more useful stuff too?

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Maxim Kirby
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        Repository repo = null;
        boolean repoExists = Repository.GITLET_DIR.exists();

        // Check if command was entered
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        // Fetch command
        String firstArg = args[0];

        // Check command and existence of repository
        if (!firstArg.equals("init")) {
            if (repoExists) {
                repo = readObject(Repository.repository, Repository.class);
            } else {
                System.out.println("Not in an initialized Gitlet directory.");
                System.exit(0);
            }
        } else {
            if (!repoExists) {
                repo = new Repository();
            } else {
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);
            }
        }

        switch(firstArg) {
            case "init":
                // java gitlet.Main init
                validateNumArgs(args, 1);
                repo.initialize();
                break;

            case "add":
                // java gitlet.Main add [file name]
                validateNumArgs(args, 2);
                repo.add(args[1]);
                break;

            case "commit":
                // java gitlet.Main commit [message]
                validateNumArgs(args, 2);
                repo.commit(args[1]);
                break;

            case "rm":
                // java gitlet.Main rm [file name]
                validateNumArgs(args, 2);
                repo.rm(args[1]);
                break;

            case "log":
                // java gitlet.Main log
                validateNumArgs(args, 1);
                repo.log();
                break;

            case "global-log":
                // java gitlet.Main global-log
                validateNumArgs(args, 1);
                repo.global_log();
                break;

            case "find":
                // java gitlet.Main find [commit message]
                validateNumArgs(args, 2);
                repo.find(args[1]);
                break;

            case "status":
                // java gitlet.Main status
                validateNumArgs(args, 1);
                repo.status();
                break;

            case "checkout": // NEED TO REORDER THESE NOT TO PRODUCE ERRORS!!!
                // java gitlet.Main checkout -- [file name]
                if (args[1].equals("--")) {
                    validateNumArgs(args, 3);
                    repo.checkout(args[2]);
                }

                // java gitlet.Main checkout [branch name]
                else if (!args[1].equals("--")) {
                    validateNumArgs(args, 2);
                    repo.checkoutBranch(args[1]);
                }

                // java gitlet.Main checkout [commit id] -- [file name]
                else if (args[2].equals("--")){
                    validateNumArgs(args, 4);
                    repo.checkout(args[3], args[1]);
                }

                else { // Incorrect formatting of operands
                    validateNumArgs(args, 0);
                }
                break;

            case "branch":
                // java gitlet.Main branch [branch name]
                validateNumArgs(args, 2);
                repo.branch(args[1]);
                break;

            case "rm-branch":
                // java gitlet.Main rm-branch [branch name]
                validateNumArgs(args, 2);
                repo.rmBranch(args[1]);
                break;

            case "reset":
                // java gitlet.Main reset [commit id]
                validateNumArgs(args, 2);
                repo.reset(args[1]);
                break;

            case "merge":
                // java gitlet.Main merge [branch name]
                validateNumArgs(args, 2);
                repo.merge(args[1]);
                break;

            // push & pull are extra credit

            case "test":
                repo.testMerging();
                break;

            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands."); // also has to be done for incorrect formatting of operands
            System.exit(0);
        }
    }
}

