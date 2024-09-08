package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.DataStructures.LocationData;
import com.darkere.crashutils.DataStructures.TileEntityData;
import com.darkere.crashutils.DataStructures.WorldPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class NetworkTools {

    public static boolean returnOnNull(Object... objects) {
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }
}