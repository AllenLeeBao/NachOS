package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {

    private TreeSet<WaitThread> waitThreads;

    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        waitThreads = new TreeSet<>();
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() { timerInterrupt(); }
            });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	    //KThread.currentThread().yield();
        long currentTime = Machine.timer().getTime();
        while ((!waitThreads.isEmpty()) && waitThreads.first().time <= currentTime) {
            waitThreads.first().kThread.ready();
            waitThreads.remove(waitThreads.first());
        }
        return;
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
        /*long wakeTime = Machine.timer().getTime() + x;
        while (wakeTime > Machine.timer().getTime())
            KThread.yield();*/

        //New Code
        long awakeTime = Machine.timer().getTime() + x;
        WaitThread waitThread = new WaitThread(awakeTime, KThread.currentThread());
        boolean intStatus = Machine.interrupt().disable();
        waitThreads.add(waitThread);
        KThread.sleep();
        Machine.interrupt().restore(intStatus);
    }

    private class WaitThread implements Comparable{
        long time;
        KThread kThread;
        public WaitThread(long time, KThread kThread) {
            this.time = time;
            this.kThread = kThread;
        }

        public int compareTo(Object obj) {
            WaitThread that = (WaitThread)obj;
            if (this.time < that.time)
                return -1;
            if (this.time > that.time)
                return 1;
            return this.kThread.compareTo(that.kThread);
        }
    }
}
