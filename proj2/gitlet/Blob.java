package gitlet;

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    public String id;
    public String name;
    public byte[] content;

    public Blob(File f, String n) {
        name = n;
        content = readContents(f);
        id = sha1(name, content);
        System.out.println("Blob constructed: " + id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
