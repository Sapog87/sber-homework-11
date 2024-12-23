package helper;

public class KnowUsedThreadRunnable implements Runnable {

    private final Runnable runnable;
    private Thread usedThread;

    public KnowUsedThreadRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        usedThread = Thread.currentThread();
        runnable.run();
    }

    public Thread getUsedThread() {
        return usedThread;
    }
}
