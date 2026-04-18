package com.darkere.crashutils.Network;

import java.util.Arrays;
import java.util.Objects;

public class NetworkTools {

    public static boolean returnOnNull(Object... objects) {
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }
}