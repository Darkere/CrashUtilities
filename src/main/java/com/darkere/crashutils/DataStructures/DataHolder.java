package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.UpdateDataRequestMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class DataHolder {
    private static final LinkedList<LoadedChunkData> LOADED_CHUNK_DATA_HOLDER = new LinkedList<>();
    private static final LinkedList<EntityData> ENTITY_DATA_HOLDER = new LinkedList<>();
    private static final LinkedList<TileEntityData> TILE_ENTITY_DATA_HOLDER = new LinkedList<>();
    private static final LinkedList<PlayerData> PLAYER_DATA_HOLDER = new LinkedList<>();
    private static Timer timer = new Timer();
    private static Runnable listener = null;
    private static Map<String, Set<ChunkPos>> stateData;
    private static Map<String, Set<ChunkPos>> ticketData;
    private static DataRequestType currentDataType;


    public static void addPlayerData(PlayerData data) {
        if (PLAYER_DATA_HOLDER.size() > 3) {
            PLAYER_DATA_HOLDER.removeLast();
        }
        PLAYER_DATA_HOLDER.addFirst(data);
        notifyListener();
    }

    public static PlayerData getLatestPlayerData() {
        return (PlayerData) getLatestData(PLAYER_DATA_HOLDER);
    }

    //----------------------------------------------------------------------------------------------------------------
    public static void addStateData(Map<String, Set<ChunkPos>> data) {
        if (ticketData == null) {
            stateData = data;
        } else {
            addLoadedChunkData(new LoadedChunkData(ticketData, data));
            ticketData = null;
            stateData = null;
        }
    }

    public static void addTicketData(Map<String, Set<ChunkPos>> data) {
        if (stateData == null) {
            ticketData = data;
        } else {
            addLoadedChunkData(new LoadedChunkData(data, stateData));
            ticketData = null;
            stateData = null;
        }
    }

    public static void addLoadedChunkData(LoadedChunkData data) {
        if (LOADED_CHUNK_DATA_HOLDER.size() > 3) {
            LOADED_CHUNK_DATA_HOLDER.removeLast();
        }
        LOADED_CHUNK_DATA_HOLDER.addFirst(data);
        data.createReverseMapping();
        notifyListener();
    }

    public static LoadedChunkData getLatestChunkData() {
        return (LoadedChunkData) getLatestData(LOADED_CHUNK_DATA_HOLDER);
    }

//---------------------------------------------------------------------------------

    public static void addEntityData(EntityData data) {
        if (ENTITY_DATA_HOLDER.size() > 3) {
            ENTITY_DATA_HOLDER.removeLast();
        }
        ENTITY_DATA_HOLDER.addFirst(data);
        notifyListener();
    }

    public static EntityData getLatestEntityData() {
        return (EntityData) getLatestData(ENTITY_DATA_HOLDER);
    }

//---------------------------------------------------------------------------------

    public static void addTileEntityData(TileEntityData data) {
        if (TILE_ENTITY_DATA_HOLDER.size() > 3) {
            TILE_ENTITY_DATA_HOLDER.removeLast();
        }
        TILE_ENTITY_DATA_HOLDER.addFirst(data);
        notifyListener();
    }

    public static TileEntityData getLatestTileEntityData() {
        return (TileEntityData) getLatestData(TILE_ENTITY_DATA_HOLDER);
    }

//---------------------------------------------------------------------------------

    private static Object getLatestData(LinkedList<?> list) {
        if (!list.isEmpty()) {
            return list.peek();
        }
        return null;
    }

    public static void requestImmediateUpdate(ResourceKey<Level> dim) {
        Network.sendToServer(new UpdateDataRequestMessage(currentDataType, dim));
    }

    public static void requestUpdates(int updateFrequency, ResourceKey<Level> dim, boolean now) {
        timer.cancel();
        if (now) requestImmediateUpdate(dim);
        if (updateFrequency == 0) return;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Network.sendToServer(new UpdateDataRequestMessage(currentDataType, dim));
            }
        }, updateFrequency, updateFrequency);
    }

    public static void cancelTimer() {
        timer.cancel();
    }

    //--------------------------------------------------------------------------------

    public static void setListener(Runnable run) {
        listener = run;
    }

    public static void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    public static void stopListening() {
        listener = null;
    }

    public static void setRequestType(DataRequestType type) {
        currentDataType = type;
    }

    public static void resetFilters() {
        if (getLatestEntityData() != null) getLatestEntityData().resetChunkMap();
        if (getLatestTileEntityData() != null) getLatestTileEntityData().resetChunkMap();
    }
}
