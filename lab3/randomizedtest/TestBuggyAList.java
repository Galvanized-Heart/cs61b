package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import net.sf.saxon.om.Item;
import org.junit.Test;
import timingtest.AList;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void testThreeAddThreeRemove() {
      AListNoResizing<Integer> correct = new AListNoResizing<>();
      BuggyAList<Integer> broken = new BuggyAList<>();
      int x = 3;
      for (int i = 0; i < x; i+=1) {
        correct.addLast(i);
        broken.addLast(i);
      }
      assertEquals(correct.size(), broken.size());
      for (int i = 0; i < x; i+=1) {
        assertEquals(correct.removeLast(), broken.removeLast());
      }
    }

    @Test
    public void randomizedTest() {
      AListNoResizing<Integer> correct = new AListNoResizing<>();
      BuggyAList<Integer> broken = new BuggyAList<>();

      int N = 5000;
      for (int i = 0; i < N; i += 1) {
        int operationNumber = StdRandom.uniform(0, 4);
        if (operationNumber == 0) {
          // addLast
          int randVal = StdRandom.uniform(0, 100);
          correct.addLast(randVal);
          broken.addLast(randVal);
        }
        else if (operationNumber == 1) {
          // size
          int size_c = correct.size();
          int size_b = broken.size();
        }
        else if (operationNumber == 2 && correct.size() != 0) {
          // getLast
          int val_c = correct.getLast();
          int val_b = broken.getLast();
          assertEquals(val_c, val_b);
        }
        else if (operationNumber == 3 && correct.size() != 0) {
          // removeLast
          int val2_c = correct.removeLast();
          int val2_b = broken.removeLast();
          assertEquals(val2_c, val2_b);
        }
      }
    }
}

