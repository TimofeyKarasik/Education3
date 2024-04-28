package edu.innotech;


import java.util.concurrent.CopyOnWriteArraySet;

public class CacheCleaner implements Runnable {
    private static CacheCleaner cleaner;
    private static Thread job;
    private int timeout;
    private CopyOnWriteArraySet<Cleanable> cleanList = new CopyOnWriteArraySet<>();
    private boolean goWork = true;

    private CacheCleaner(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void run() {
        while (goWork) {
            for (Cleanable cache : cleanList) {
                if (cache.clean()) {
                    cleanList.remove(cache);
                }
                ;
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void submitCleaning(Cleanable clean) {
        cleaner = new CacheCleaner(200);
        job = new Thread(cleaner, "CacheCleaner");
        job.setDaemon(true);
        job.start();
        cleaner.cleanList.add(clean);
    }
}