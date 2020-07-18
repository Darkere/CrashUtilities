package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.WorldPos;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;

public class NetworkTools {
    public static void writeSChPMap(PacketBuffer buf, Map<String, Set<ChunkPos>> map) {
        buf.writeInt(map.size());
        map.forEach((key, value) -> {
            buf.writeString(key);
            buf.writeInt(value.size());
            value.forEach(x -> {
                buf.writeBlockPos(x.asBlockPos());
            });
        });
    }

    public static Map<String, Set<ChunkPos>> readSChPMap(PacketBuffer buf) {
        HashMap<String, Set<ChunkPos>> map = new HashMap<>();
        int mapsize = buf.readInt();
        for (int i = 0; i < mapsize; i++) {
            String key = buf.readString();
            int listsize = buf.readInt();
            Set<ChunkPos> list = new HashSet<>();
            for (int j = 0; j < listsize; j++) {
                list.add(new ChunkPos(buf.readBlockPos()));
            }
            map.put(key, list);
        }
        return map;
    }

    public static void writeWorldKey(RegistryKey<World> worldKey, PacketBuffer buf) {
        ResourceLocation loc = worldKey.func_240901_a_();
        buf.writeResourceLocation(loc);
    }

    public static RegistryKey<World> readWorldKey(PacketBuffer buf) {
        return RegistryKey.func_240903_a_(Registry.WORLD_KEY,buf.readResourceLocation());
    }

    public static void writeWorldPos(WorldPos pos, PacketBuffer buf) {
        buf.writeBlockPos(pos.pos);
        writeWorldKey(pos.type, buf);
    }

    public static WorldPos readWorldPos(PacketBuffer buf) {
        return new WorldPos(buf.readBlockPos(), readWorldKey(buf));
    }

    public static void writeRLWPMap(Map<ResourceLocation, List<WorldPos>> map, PacketBuffer buf) {
        buf.writeInt(map.size());
        map.forEach((x, y) -> {
            buf.writeResourceLocation(x);
            buf.writeInt(y.size());
            y.forEach(e -> {
                writeWorldPos(e,buf);
            });
        });
    }

    public static Map<ResourceLocation, List<WorldPos>> readRLWPMap(PacketBuffer buf) {
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