package com.darkere.crashutils.DataStructures;

import net.minecraft.profiler.DataPoint;
import net.minecraft.profiler.IProfileResult;

import java.util.List;

public class ProfilingData {

    public ProfilingData(IProfileResult result) {
        List<DataPoint> data = result.getTimes("root");
    }

}
