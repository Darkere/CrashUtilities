package com.darkere.crashutils;

import com.electronwill.nightconfig.core.utils.StringUtils;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {


    private ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    private ForgeConfigSpec.BooleanValue enabled;
    private ForgeConfigSpec.IntValue timer;
    private ForgeConfigSpec.IntValue maximum;
    private ForgeConfigSpec.ConfigValue<String> warnings;
    private ForgeConfigSpec.BooleanValue title;
    private ForgeConfigSpec.ConfigValue<String> titletext;
    private ForgeConfigSpec.ConfigValue<String> warningtext;
    private ForgeConfigSpec.IntValue memoryLogTimer;
    private ForgeConfigSpec.IntValue memoryWarnDelta;
    private ForgeConfigSpec.IntValue memoryTimer;
    private ForgeConfigSpec.BooleanValue memoryChecker;
    private ForgeConfigSpec.BooleanValue heapDump;

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
        memoryWarnDelta = builder.comment("Threshold at which the Memory checker will diplay a warning in the Log (in MB)").defineInRange("threshold", 1000, 0, Integer.MAX_VALUE);
        heapDump = builder.comment("Run /spark heapdump when memory fills up more than 95 % the first time. This value gets set to false if this occurs").define("heapdump", false);
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

    public ForgeConfigSpec getSpec() {

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
}
