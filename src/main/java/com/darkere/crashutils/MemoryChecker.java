package com.darkere.crashutils;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.LogManager;

public class MemoryChecker extends TimerTask {
    public List<MemoryCount> counts = new ArrayList<>();
    long lastlog;
    int logTimer;
    double lastUsed;
    long warnDelta;
    public void setup(){
        logTimer = CrashUtils.SERVER_CONFIG.getMemoryLogTimer() * 1000;
        warnDelta = CrashUtils.SERVER_CONFIG.getMemoryWarnDelta();

    }

    @Override
    public void run() {
        Runtime r = Runtime.getRuntime();
        MemoryCount count = new MemoryCount(r.maxMemory(), r.freeMemory(), r.totalMemory());
        if(shouldLog()){
            counts.add(count);
        }
        double used = inMegaBytes(count.getMaximum()- count.getFree());
        double delta = used -lastUsed;
        if(delta > warnDelta){
            CrashUtils.LOGGER.info("Memory Spike" + delta + " MB");
        }
        lastUsed = used;

    }

    private boolean shouldLog() {
        if((lastlog + logTimer) < System.currentTimeMillis()){
            lastlog = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static double inMegaBytes(long x){
        double y = (double) x;
        return y / 1024 / 1024;
    }
    public static double inGigaBytes(long x){
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
