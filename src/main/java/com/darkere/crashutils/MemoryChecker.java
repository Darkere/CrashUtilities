package com.darkere.crashutils;

import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MemoryChecker extends TimerTask {

    public static MemoryChecker INSTANCE;
    public List<MemoryCount> counts = new ArrayList<>();
    long lastlog;
    int logTimer;
    double lastUsed;
    long warnDelta;
    boolean ranHeapDump = false;
    boolean heapDumpEnabled;
    Timer timer;

    public static void restart(){
        if(INSTANCE != null){
            INSTANCE.shutdown();
        }
        INSTANCE = new MemoryChecker();
        INSTANCE.setup();
    }

    private void shutdown() {
        timer.cancel();
        counts.clear();
    }

    public void setup() {
        if(timer != null){
            timer.cancel();
        }

        if (CrashUtils.SERVER_CONFIG.getMemoryChecker()) {
            logTimer = CrashUtils.SERVER_CONFIG.getMemoryLogTimer() * 1000;
            warnDelta = CrashUtils.SERVER_CONFIG.getMemoryWarnDelta();
            heapDumpEnabled = CrashUtils.SERVER_CONFIG.getHeapDump();
            timer = new Timer("CU Memory Check Task", true);
            int time = CrashUtils.SERVER_CONFIG.getMemoryTimer() * 1000;
            timer.scheduleAtFixedRate(this, time, time);
        }
    }

    @Override
    public void run() {
        Runtime r = Runtime.getRuntime();
        MemoryCount count = new MemoryCount(r.maxMemory(), r.freeMemory(), r.totalMemory());
        if (shouldLog()) {
            counts.add(count);
            if (counts.size() > 100) {
                for (int i = 0; i < 20; i++) {
                    counts.remove(counts.size() - 1);
                }
            }
        }

        double used = inMegaBytes(count.getMaximum() - count.getFree());
        double delta = used - lastUsed;
        String deltaString = String.format("%.2f", delta);
        if (delta > warnDelta) {
            CrashUtils.LOGGER.info("Memory Spike " + deltaString + " MB");
        }
        lastUsed = used;
        double usedPerc = (float) count.getFree() / (float) count.getMaximum();
        if (usedPerc > 0.95F) {
            if (!ranHeapDump && CrashUtils.sparkLoaded) {
                CrashUtils.LOGGER.info("Running Spark Heapdump! LagSpike incoming!");
                CrashUtils.SERVER_CONFIG.disableHeapDump();
                ranHeapDump = true;
                CrashUtils.runNextTick((world)->{
                    world.getServer().getPlayerList().broadcastMessage(new StringTextComponent("Running Heapdump. Massive Lagspike incoming!"), ChatType.SYSTEM,Util.NIL_UUID);
                    world.getServer().getCommands().performCommand(world.getServer().createCommandSourceStack(), "/spark heapdump");
                });
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

