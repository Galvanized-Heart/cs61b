package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Repo implements Serializable {

    /** Reference to head of the master and side branches */
    public String master;
    public String branch;

    /** Reference to current Commit */
    public String HEAD;

    /** Mapping of SHA-1 Strings to all other Objects required for Gitlet */
    public HashMap<String, Object> references = new HashMap<String, Object>();

    /***************************************************************************************************/


    /** Returns serialized object based on SHA-1 ID. */
    public Object sha1ToObj(String sha, Object objType) {
        return references.get(sha);
    }

    /** Updates references HashMap for new Objects values using SHA-1 keys. */
    public void setReferences(String sha, Object obj) {
        references.put(sha, obj);
    }

}
