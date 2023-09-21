package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int[] nums = {1000, 2000, 4000, 8000, 16000, 32000, 64000};
        SLList<Integer> dummy = new SLList<>();
        int num_ops = 10000;
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> ops = new AList<>();

        for (int i = 0; i < nums.length; i+=1) {
            ops.addLast(num_ops);
            for (int j = 0; j < nums[i]; j+=1) {
                dummy.addLast(1);
            }
            Stopwatch sw = new Stopwatch();
            for (int k = 0; k < num_ops; k+=1) {
                dummy.getLast();
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
            Ns.addLast(nums[i]);
        }
        printTimingTable(Ns, times, ops);
    }

}
