package com.darkere.crashutils.DataStructures;

import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ResultField;

import java.util.List;

public class ProfilingData {

    public ProfilingData(ProfileResults result) {
        List<ResultField> data = result.getTimes("root");
    }

}
