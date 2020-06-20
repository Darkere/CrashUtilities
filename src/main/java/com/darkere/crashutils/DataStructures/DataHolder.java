package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.UpdateDataRequestMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class DataHolder {
    private static final LinkedList<LoadedChunkData> LOADED_CHUNK_DATA_HOLDER = new LinkedList<>();
    private static final LinkedList<EntityData> ENTITY_DATA_HOLDER = new LinkedList<>();
    private static final LinkedList<TileEntityData> TILE_ENTITY_DATA_HOLDER = new LinkedList<>();
    private static final LinkedList<PlayerData> PLAYER_DATA_HOLDER = new LinkedList<>();
    private static Timer timer = new Timer();
    private static ResourceLocation entityFilter = null;
    private static ResourceLocation tileEntityFilter = null;
    private static Runnable listener = null;
    private static String ChunkDataFilter = "";

    public static void addPlayerData(PlayerData data){
        if(PLAYER_DATA_HOLDER.size() > 3){
            PLAYER_DATA_HOLDER.removeLast();
        }
        PLAYER_DATA_HOLDER.addFirst(data);
        notifyListener();
    }

    public static PlayerData getLatestPlayerData(){
        return(PlayerData)getLatestData(PLAYER_DATA_HOLDER);
    }

//----------------------------------------------------------------------------------------------------------------
    public static void addLoadedChunkData(LoadedChunkData data) {
        if (LOADED_CHUNK_DATA_HOLDER.size() > 3) {
            LOADED_CHUNK_DATA_HOLDER.removeLast();
        }
        LOADED_CHUNK_DATA_HOLDER.addFirst(data);
        data.createReverseMapping();
        data.applyFilter(ChunkDataFilter);
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
        data.fillChunkMap(entityFilter);
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
        data.fillChunkMap(tileEntityFilter);
        notifyListener();
    }

    public static TileEntityData getLatestTileEntityData() {
        return (TileEntityData) getLatestData(TILE_ENTITY_DATA_HOLDER);
    }

//---------------------------------------------------------------------------------

    private static Object getLatestData(LinkedList list) {
        if (list.size() > 0) {
            return list.peek();
        }
        return null;
    }

    public static void requestUpdates(DataRequestType type, int updateFrequency, DimensionType dim, boolean now) {
        timer.cancel();
        if (now) Network.INSTANCE.sendToServer(new UpdateDataRequestMessage(type, dim));
        if (updateFrequency == 0) return;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Network.INSTANCE.sendToServer(new UpdateDataRequestMessage(type, dim));
            }
        }, updateFrequency, updateFrequency);
    }

    public static void cancelTimer() {
        timer.cancel();
    }

 //--------------------------------------------------------------------------------
    public static void setChunkDataFilter(String s){
        if(getLatestChunkData() == null)return;
        ChunkDataFilter = s;
        getLatestChunkData().applyFilter(ChunkDataFilter);
    }

    public static void setEntityFilter(String s) {
        if(getLatestEntityData() == null)return;
        entityFilter = s == null ? null : new ResourceLocation(s);
        getLatestEntityData().fillChunkMap(entityFilter);
    }

    public static void setTileEntityFilter(String s) {
        if(getLatestTileEntityData() == null)return;
        tileEntityFilter = s == null ? null : new ResourceLocation(s);
        getLatestTileEntityData().fillChunkMap(tileEntityFilter);
    }
    public static void registerListener(Runnable run){
        listener = run;
    }
    public static void notifyListener(){
        listener.run();
    }
    public static void stopListening(){
        listener = null;
    }
}
