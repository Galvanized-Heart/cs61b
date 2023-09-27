package flik;

/** An Integer tester created by Flik Enterprises.
 * @author Josh Hug
 * */
public class Flik {
    /** @param a Value 1
     *  @param b Value 2
     *  @return Whether a and b are the same */
    public static boolean isSameNumber(Integer a, Integer b) {
        return a.equals(b);
        // Java uses int caching for int values -128 to 127
        // So when a < 128 and b < 128, == no longer works
        // equals() needs to be used instead
    }
}
