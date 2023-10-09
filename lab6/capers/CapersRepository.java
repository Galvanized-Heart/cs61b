package capers;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.IOException;

import static capers.Utils.*;

/** A repository for Capers 
 * @author Maxim Kirby
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 */
public class CapersRepository {
    /**
     * Current Working Directory.
     */
    static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * Main metadata folder.
     */
    static final File CAPERS_FOLDER = join(CWD, ".capers");

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * Remember: recommended structure (you do not have to follow):
     * <p>
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     * - dogs/ -- folder containing all of the persistent data for dogs
     * - story -- file containing the current story
     */
    public static void setupPersistence() {
        File dogDir = join(CAPERS_FOLDER, "dogs");
        File storyFile = join(CAPERS_FOLDER, "story.txt");
        CAPERS_FOLDER.mkdir();
        dogDir.mkdir();
        try {
            storyFile.createNewFile();
        }
        catch (IOException e) {
            System.err.println("Error creating the file: " + e.getMessage());
            System.out.println(storyFile);
            System.out.println(dogDir);
        }
    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     *
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        File storyFile = join(CAPERS_FOLDER, "story.txt");
        String currStory = readContentsAsString(storyFile) + text + "\n";
        System.out.println(currStory);
        writeContents(storyFile, currStory);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        Dog dog = new Dog(name, breed, age);
        System.out.println(dog.toString());
        dog.saveDog();
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     *
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        Dog dog = Dog.fromFile(name);
        dog.haveBirthday();
        dog.saveDog();
    }

    /**
     * Deletes the files used to save dogs and story.
     */
    public static void reset() {
        File story = join(CAPERS_FOLDER, "story.txt");
        if (story.exists()) {
            story.delete();
        }
        File dogDir = join(CAPERS_FOLDER, "dogs");
        if (dogDir.exists()) {
            File[] dogs = dogDir.listFiles();
            if (dogs != null) {
                for (File file : dogs) {
                    file.delete();
                }
            }
            dogDir.delete();
        }
    }
}
