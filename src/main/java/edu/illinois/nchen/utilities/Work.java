package edu.illinois.nchen.utilities;

import com.google.common.base.Stopwatch;

/**
 * Based on Microsoft.Practice.ParallelGuideSamples.Utilities.SampleUtilities
 * Adapted to Java.
 *
 * @author Nicholas Chen
 */
public class Work {

    public static void performCPUOperation(float seconds) {
        long milliseconds = (long) (1000 * seconds);

        Stopwatch watch = new Stopwatch().start();

        long checkInterval = Math.min(20000000, (long) (20000000 * seconds));

        int i = 0;
        while (true) {
            i += 1;

            if (i % checkInterval == 0) {
                if (watch.elapsedMillis() > milliseconds) {
                    return;
                }
            }
        }
    }

    // vary to simulate I/O jitter
    static int[] sleepTimeouts = new int[]{65, 165, 110, 110, 185, 160, 40, 125, 275, 110, 80, 190, 70, 165, 80, 50, 45,
            155, 100, 215, 85, 115, 180, 195, 135, 265, 120, 60, 130, 115, 200, 105, 310, 100, 100, 135, 140, 235, 205,
            10, 95, 175, 170, 90, 145, 230, 365, 340, 160, 190, 95, 125, 240, 145, 75, 105, 155, 125, 70, 325, 300, 175,
            155, 185, 255, 210, 130, 120, 55, 225, 120, 65, 400, 290, 205, 90, 250, 245, 145, 85, 140, 195, 215, 220,
            130, 60, 140, 150, 90, 35, 230, 180, 200, 165, 170, 75, 280, 150, 260, 105};

    public static void performIOOperation(float seconds) throws InterruptedException {
        long milliseconds = (long) (1000 * seconds);

        Stopwatch watch = new Stopwatch().start();

        int timeoutCount = sleepTimeouts.length;

        int i = (Math.abs(watch.hashCode())) % timeoutCount;
        while (true) {
            int timeout = sleepTimeouts[i];
            i += 1;
            i = i % timeoutCount;

            Thread.sleep(timeout);

            if (watch.elapsedMillis() > milliseconds) {
                return;
            }

        }


    }
}
