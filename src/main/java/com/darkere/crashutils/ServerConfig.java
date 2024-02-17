package com.darkere.crashutils;

import com.electronwill.nightconfig.core.utils.StringUtils;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {


    private ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private ModConfigSpec.BooleanValue enabled;
    private ModConfigSpec.IntValue timer;
    private ModConfigSpec.IntValue maximum;
    private ModConfigSpec.ConfigValue<String> warnings;
    private ModConfigSpec.BooleanValue title;
    private ModConfigSpec.ConfigValue<String> titletext;
    private ModConfigSpec.ConfigValue<String> warningtext;
    private ModConfigSpec.IntValue memoryLogTimer;
    private ModConfigSpec.IntValue memoryWarnDelta;
    private ModConfigSpec.IntValue memoryTimer;
    private ModConfigSpec.BooleanValue memoryChecker;
    private ModConfigSpec.BooleanValue heapDump;
    private ModConfigSpec.IntValue chunkExpire;
    private ModConfigSpec.BooleanValue shouldChunksExpire;

    ServerConfig() {

        builder.push("Item Clear");
        builder.comment("Check Every <timer>  minutes how many items are loaded. If there are more than <maximum>. Then Remove All Items. Configs are reloaded on worldreload and clear");
        enabled = builder.comment("Enable regular ItemClear").define("enabled", false);
        timer = builder.comment("Waiting time between Checks in Minutes").defineInRange("timer", 30, 0, 1440);
        maximum = builder.comment("Do a clear if there are more than X items").defineInRange("maximum", 1000, 0, Integer.MAX_VALUE);
        warnings = builder.comment("Chat warning, Comma separated, in seconds").define("warnings", "5,20");
        title = builder.comment("Run a Title command on first warning").define("title", true);
        titletext = builder.comment("Text to display in TitleCommand").define("titletext", "ITEMCLEAR INCOMING!");
        warningtext = builder.comment("Text to Display in Chat when Item. First % will be replaced by Value. In red, encased in [=== ").define("warningtext", "ITEMCLEAR IN % SECONDS");
        builder.pop();
        builder.push("Memory Checker");
        builder.comment("Adds a Memory checker that reads currently used Memory. A command that reads out the last logged memory values, and a warning in logs when large amounts of memory get used in a small amount of time");
        memoryChecker = builder.comment("Enable the Memory checker").define("enabled", false);
        memoryTimer = builder.comment("Check memory every (in seconds)").defineInRange("timer", 5, 0, Integer.MAX_VALUE);
        memoryLogTimer = builder.comment("Time between Memory Checks that will get saved for display(in seconds)").defineInRange("timer", 30, 0, Integer.MAX_VALUE);
        memoryWarnDelta = builder.comment("Threshold at which the Memory checker will display a warning in the Log (in MB)").defineInRange("threshold", 1000, 0, Integer.MAX_VALUE);
        heapDump = builder.comment("Run /spark heapdump when memory fills up more than 95 % the first time. This value gets set to false if this occurs").define("heapdump", false);
        builder.pop();
        builder.push("Ftb Chunks");
        shouldChunksExpire = builder.comment("If Ftb chunks is installed enable automatic purge of loaded chunks for people who have not been online for some amount of days. (Note LOADED not Claimed Chunks)").define("enabled",false);
        chunkExpire = builder.comment("Number of days after which a players chunks will be unloaded. Warning! This relies on the modify date of the player data file. ").defineInRange("days",7,0,Integer.MAX_VALUE);
        builder.pop();

    }

    public boolean getEnabled() {
        return enabled.get();
    }

    public int getTimer() {
        return timer.get();
    }

    public int getMaximum() {
        return maximum.get();
    }

    public List<Integer> getWarnings() {
        List<Integer> ints = new ArrayList<>();
        StringUtils.split(warnings.get(), ',').forEach(x -> ints.add(Integer.parseInt(x)));
        return ints;
    }

    public boolean getTitle() {
        return title.get();
    }

    public ModConfigSpec getSpec() {

        return builder.build();
    }

    public String getWarningText() {
        return warningtext.get();
    }

    public String getTitleText() {
        return titletext.get();
    }

    public int getMemoryLogTimer() {
        return memoryLogTimer.get();
    }

    public int getMemoryWarnDelta() {
        return memoryWarnDelta.get();
    }

    public int getMemoryTimer() {
        return memoryTimer.get();
    }

    public boolean getMemoryChecker() {
        return memoryChecker.get();
    }

    public boolean getHeapDump() {
        return heapDump.get();
    }

    public void disableHeapDump() {
        heapDump.set(false);
    }

    public boolean shouldChunksExpire(){return shouldChunksExpire.get();}
    public int getExpireTimeInDays(){return chunkExpire.get();}
}
