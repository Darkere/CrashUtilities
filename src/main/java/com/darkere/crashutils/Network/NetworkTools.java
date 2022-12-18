package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.WorldPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class NetworkTools {
    public static void writeSChPMap(FriendlyByteBuf buf, Map<String, Set<ChunkPos>> map) {
        buf.writeInt(map.size());
        map.forEach((key, value) -> {
            buf.writeUtf(key);
            buf.writeInt(value.size());
            value.forEach(x -> {
                buf.writeLong(x.toLong());
            });
        });
    }

    public static Map<String, Set<ChunkPos>> readSChPMap(FriendlyByteBuf buf) {
        HashMap<String, Set<ChunkPos>> map = new HashMap<>();
        int mapsize = buf.readInt();
        for (int i = 0; i < mapsize; i++) {
            String key = buf.readUtf();
            int listsize = buf.readInt();
            Set<ChunkPos> list = new HashSet<>();
            for (int j = 0; j < listsize; j++) {
                list.add(new ChunkPos(buf.readLong()));
            }
            map.put(key, list);
        }
        return map;
    }

    public static void writeWorldKey(ResourceKey<Level> worldKey, FriendlyByteBuf buf) {
        ResourceLocation loc = worldKey.location();
        buf.writeResourceLocation(loc);
    }

    public static ResourceKey<Level> readWorldKey(FriendlyByteBuf buf) {
        return ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
    }

    public static void writeWorldPos(WorldPos pos, FriendlyByteBuf buf) {
        buf.writeBlockPos(pos.pos);
        writeWorldKey(pos.type, buf);
        buf.writeUUID(pos.id);
    }

    public static WorldPos readWorldPos(FriendlyByteBuf buf) {
        return new WorldPos(buf.readBlockPos(), readWorldKey(buf), buf.readUUID());
    }

    public static void writeRLWPMap(Map<ResourceLocation, List<WorldPos>> map, FriendlyByteBuf buf) {
        buf.writeInt(map.size());
        map.forEach((x, y) -> {
            buf.writeResourceLocation(x);
            buf.writeInt(y.size());
            y.forEach(e -> {
                writeWorldPos(e, buf);
            });
        });
    }

    public static Map<ResourceLocation, List<WorldPos>> readRLWPMap(FriendlyByteBuf buf) {
        Map<ResourceLocation, List<WorldPos>> map = new HashMap<>();
        int mapsize = buf.readInt();
        for (int i = 0; i < mapsize; i++) {
            ResourceLocation loc = buf.readResourceLocation();
            int listsize = buf.readInt();
            List<WorldPos> list = new ArrayList<>();
            for (int j = 0; j < listsize; j++) {
                list.add(NetworkTools.readWorldPos(buf));
            }
            map.put(loc, list);
        }
        return map;
    }

    public static boolean returnOnNull(Object... objects) {
        return Arrays.stream(objects).anyMatch(Objects::isNull);
    }
}

//TEMPLATE
//    public XXXXXXXXXXXXXXXXXX() {
//
//    }
//
//
//    public static void encode(XXXXXXXXXXXXXXXXXX data, PacketBuffer buf) {
//
//    }
//
//
//    public static XXXXXXXXXXXXXXXXXX decode(PacketBuffer buf) {
//        return new XXXXXXXXXXXXXXXXXX(
//
//        );
//    }
//
//    public static boolean handle(XXXXXXXXXXXXXXXXXX data, Supplier<NetworkEvent.Context> ctx) {
//        ctx.get().enqueueWork(() -> {
//
//        });
//        return true;
//    }