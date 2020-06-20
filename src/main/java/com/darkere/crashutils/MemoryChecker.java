package com.darkere.crashutils;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class MemoryChecker extends TimerTask {
    public List<MemoryCount> counts = new ArrayList<>();
    long lastlog;
    int logTimer;
    double lastUsed;
    long warnDelta;
    boolean ranHeapDump = false;
    boolean heapDumpEnabled;

    public void setup() {
        logTimer = CrashUtils.SERVER_CONFIG.getMemoryLogTimer() * 1000;
        warnDelta = CrashUtils.SERVER_CONFIG.getMemoryWarnDelta();
        heapDumpEnabled = CrashUtils.SERVER_CONFIG.getHeapDump();

    }

    @Override
    public void run() {
        Runtime r = Runtime.getRuntime();
        MemoryCount count = new MemoryCount(r.maxMemory(), r.freeMemory(), r.totalMemory());
        if (shouldLog()) {
            counts.add(count);
        }

        double used = inMegaBytes(count.getMaximum() - count.getFree());
        double delta = used - lastUsed;
        String deltaString = String.format("%.2f", delta);
        if (delta > warnDelta) {
            CrashUtils.LOGGER.info("Memory Spike " + deltaString + " MB");
        }
        lastUsed = used;
        double usedPerc = (float)count.getFree()/(float)count.getMaximum();
        if(usedPerc > 0.95F){
            if(!ranHeapDump && CrashUtils.sparkLoaded){
                CrashUtils.runHeapDump =true;
            }
            CrashUtils.LOGGER.info("Memory full, using" + usedPerc + "% of memory");
        }

    }

    private boolean shouldLog() {
        if ((lastlog + logTimer) < System.currentTimeMillis()) {
            lastlog = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static double inMegaBytes(long x) {
        double y = (double) x;
        return y / 1024 / 1024;
    }

    public static double inGigaBytes(long x) {
        double y = (double) x;
        return y / 1024 / 1024 / 1024;
    }


    public static class MemoryCount {
        private long maximum;
        private long free;
        private long total;

        public MemoryCount(long maximum, long free, long total) {
            this.maximum = maximum;
            this.free = free;
            this.total = total;
        }

        public long getMaximum() {
            return maximum;
        }

        public long getFree() {
            return free;
        }

        public long getTotal() {
            return total;
        }
    }
}

