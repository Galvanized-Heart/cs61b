package gitlet;

import java.io.File;
// maybe java.nio.file.Files
// maybe java.io and java.nio have more useful stuff too?

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Maxim Kirby
 */
public class Main {

    // Branches? Here we need to init a master branch and have it point to initial commit
    // What is UID? User ID



    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];

        // "Incorrect operands." System.exit(0) if incorrect number of args passed to case

        // "Not in an initialized Gitlet directory." System.exit(0) if commands requires
        // .gitlet directory
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs(args, 1);
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateNumArgs(args, 2);
                break;
            case "commit":
                break;
            case "rm":
                break;
            case "log":
                break;
            case "global-log":
                break;
            case "find":
                break;
            case "status":
                break;
            case "checkout":
                break;
            case "branch":
                break;
            case "rm-branch":
                break;
            case "reset":
                break;
            case "merge":
                break;
        }
        System.out.println("No command with that name exists.");
        System.exit(0);
        // "No command with that name exists." System.exit(0)
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Invalid operands.");
            System.exit(0);
        }
    }


    // Have this in another files (helpers.java?)
    public void init() {
        // Get current working directory
        File cwd = new File(System.getProperty("user.dir"));
        Commit initial = new Commit("initial commit", null);
        return;
    }
}
