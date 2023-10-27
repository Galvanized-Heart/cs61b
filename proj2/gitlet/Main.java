package gitlet;

import java.io.File;
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
                validateNumArgs(args, 1);
                if (!repoExists) {
                    Repository.initialize();
                    return;
                }
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);

            case "add":
                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.add(args[1]);
                    return;
                }
                notInit();

            case "commit":
                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.commit(args[1]);
                    return;
                }
                notInit();

            case "rm":
                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.rm(args[1]);
                    return;
                }
                notInit();

            case "log":
                validateNumArgs(args, 1);
                if (repoExists) {
                    Repository.log();
                    return;
                }
                notInit();

            case "global-log":
                validateNumArgs(args, 1);
                if (repoExists) {
                    Repository.global_log();
                    return;
                }
                notInit();

            case "find":
                validateNumArgs(args, 2);
                if (repoExists) {
                    Repository.find(args[1]);
                    return;
                }
                notInit();

            case "status":
                return;
            case "checkout": // no checkout?? See EDITED 3/5
                return;
            case "branch":
                return;
            case "rm-branch":
                return;
            case "reset":
                return;
            case "merge":
                return;
            // push & pull are extra credit
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

