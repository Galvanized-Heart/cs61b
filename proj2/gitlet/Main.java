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
        boolean repoExists = Repository.GITLET_DIR.exists();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];

        switch(firstArg) {
            case "init":
                // java gitlet.Main init

                validateNumArgs(args, 1);
                if (!repoExists) {
                    Repository.initialize();
                    return;
                }
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);

            case "add":
                // java gitlet.Main add [file name]

                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.add(args[1]);
                    return;
                }
                notInit();

            case "commit":
                // java gitlet.Main commit [message]

                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.commit(args[1]);
                    return;
                }
                notInit();

            case "rm":
                // java gitlet.Main rm [file name]

                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.rm(args[1]);
                    return;
                }
                notInit();

            case "log":
                // java gitlet.Main log

                validateNumArgs(args, 1);
                if (repoExists) {
                    Repository.log();
                    return;
                }
                notInit();

            case "global-log":
                // java gitlet.Main global-log

                validateNumArgs(args, 1);
                if (repoExists) {
                    Repository.global_log();
                    return;
                }
                notInit();

            case "find":
                // java gitlet.Main find [commit message]

                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.find(args[1]);
                    return;
                }
                notInit();

            case "status":
                // java gitlet.Main status

                validateNumArgs(args, 1);
                if (repoExists) {
                    Repository.status();
                    return;
                }
                notInit();

            case "checkout":
                // java gitlet.Main checkout -- [file name]

                // java gitlet.Main checkout [commit id] -- [file name]

                // java gitlet.Main checkout [branch name]

                // validateNumArgs(args, 2); not sure how to handle this
                if (repoExists) {
                    if (args[1].equals("--")) {
                        System.out.println("not here");
                        Repository.checkout(args[2]);
                    } else if (!args[1].equals("--")) {
                        Repository.checkoutBranch(args[1]);
                    } else if (args[2].equals("--")){
                        System.out.println("here");
                        Repository.checkout(args[3], args[1]);
                    }
                    return;
                }
                notInit();

            case "branch":
                // java gitlet.Main branch [branch name]

                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.branch(args[1]);
                    return;
                }
                notInit();

            case "rm-branch":
                // java gitlet.Main rm-branch [branch name]

                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.branch(args[1]);
                    return;
                }
                notInit();

            case "reset":
                return;
            case "merge":
                return;
            // push & pull are extra credit
            case "test":
                Repository.test();
                return;
        }
        System.out.println("No command with that name exists.");
        System.exit(0);
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands."); // also has to be done for incorrect formatting of operands
            System.exit(0);
        }
    }

    public static void notInit() {
        System.out.println("Not in an initialized Gitlet directory.");
        System.exit(0);
    }
}

