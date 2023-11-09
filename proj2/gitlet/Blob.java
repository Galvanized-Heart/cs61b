package gitlet;

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    // TODO: Make instance variables private and create methods for access

    public String id;
    public String name;
    public byte[] content;

    public Blob(File f, String n) {
        name = n;
        content = readContents(f);
        id = sha1(name, content);
    }
}
