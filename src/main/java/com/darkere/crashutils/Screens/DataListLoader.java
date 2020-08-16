package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.*;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.PlayerInventoryRequestMessage;
import com.darkere.crashutils.Network.TeleportToPlayerMessage;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class DataListLoader {
    int XTopLeft;
    int YTopLeft;
    int XAcross;
    int YAcross;
    CUList currentList;
    Comparator<CUOption> comp = Comparator.comparingInt(x -> x.number);
    Consumer<List<CUOption>> numberComparer = sort -> sort.sort(comp.reversed());
    Consumer<List<CUOption>> stringSorter = sort -> sort.sort(Comparator.comparing(CUOption::getString));
    Comparator<CUOption> compX = Comparator.comparingInt(x -> x.blockPos.getX());
    Consumer<List<CUOption>> positionSorter = option -> option.sort(compX.thenComparingInt(x -> x.getBlockPos().getZ()));
    CUScreen parent;
    RegistryKey<World> world;
    Consumer<CUOption> tpAction = option -> {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        WorldUtils.teleportPlayer(player, player.getEntityWorld(), player.getEntityWorld(), option.blockPos);
    };

    public DataListLoader(int XTopLeft, int YTopLeft, int XAcross, int YAcross, CUScreen screen, RegistryKey<World> world) {
        this.XTopLeft = XTopLeft;
        this.YTopLeft = YTopLeft;
        this.XAcross = XAcross;
        this.YAcross = YAcross;
        parent = screen;
        this.world = world;
        setCurrentList(new ArrayList<>(), null, null, false);
    }

    private void setCurrentList(List<CUOption> list, Consumer<List<CUOption>> sorter, Consumer<CUOption> action, boolean update) {
        if (update) {
            currentList.updateOptions(list, sorter, action);
        } else {
            currentList = new CUList(list, XTopLeft, YTopLeft, XAcross, YAcross, parent, sorter, action);
        }
    }

    private void setReloadListener(DataRequestType type, Runnable listener) {
        DataHolder.setRequestType(type);
        DataHolder.requestUpdates(0, world, true);
        DataHolder.setListener(listener);

    }

    public void loadOrderedEntityList(boolean update) {
        if (!update) setReloadListener(DataRequestType.ENTITYDATA, () -> loadOrderedEntityList(true));
        EntityData data = DataHolder.getLatestEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptions();
        list.forEach(option -> {
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all entities of this type", x, y, parent),
                x -> WorldUtils.removeEntityType(Minecraft.getInstance().world, option.rl, false));
            option.addButton("Wipe",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Forcefully remove all entities of this type", x, y, parent),
                x -> WorldUtils.removeEntityType(Minecraft.getInstance().world, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadChunkListForEntity(option.getRl(), false);
        setCurrentList(list, numberComparer, action, update);
    }

    public void loadChunkListForEntity(ResourceLocation name, boolean update) {
        if (!update) setReloadListener(DataRequestType.ENTITYDATA, () -> loadChunkListForEntity(name, true));
        EntityData data = DataHolder.getLatestEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptionsOfType(name);
        list.forEach(option -> {
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeEntitiesInChunk(Minecraft.getInstance().world, option.chunkPos, option.rl, false));
            option.addButton("Wipe",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Forcefully remove all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeEntitiesInChunk(Minecraft.getInstance().world, option.chunkPos, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadEntitiesInChunkAsList(option.chunkPos, name, false);
        setCurrentList(list, numberComparer, action, update);
    }

    public void loadEntitiesInChunkAsList(ChunkPos chunkPos, ResourceLocation name, boolean update) {
        if (!update)
            setReloadListener(DataRequestType.ENTITYDATA, () -> loadEntitiesInChunkAsList(chunkPos, name, true));
        EntityData data = DataHolder.getLatestEntityData();
        if (data == null) return;
        List<CUOption> list = data.getInChunkAsCUOptions(chunkPos, name);
        list.forEach(option -> {
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to Entity", x, y, parent),
                x -> tpAction.accept(option));
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove this Entity", x, y, parent),
                x -> WorldUtils.removeEntity(Minecraft.getInstance().world, option.id, false));
            option.addButton("Wipe",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Forcefully remove this Entity", x, y, parent),
                x -> WorldUtils.removeEntity(Minecraft.getInstance().world, option.id, true));
        });
        setCurrentList(list, positionSorter, null, update);
    }

    public void loadOrderedTileEntityList(boolean update) {
        if (!update) setReloadListener(DataRequestType.TILEENTITYDATA, () -> loadOrderedTileEntityList(true));
        TileEntityData data = DataHolder.getLatestTileEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptions();
        list.forEach(option -> {
            option.addButton("Remove TE",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all loaded TileEntities of this type", x, y, parent),
                b -> WorldUtils.removeTileEntityType(Minecraft.getInstance().world, option.rl, false));
            option.addButton("Remove Block", (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Delete Blocks with this loaded TileEntity", x, y, parent),
                b -> WorldUtils.removeTileEntityType(Minecraft.getInstance().world, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadChunkListForTileEntity(option.rl, false);
        setCurrentList(list, numberComparer, action, update);
    }

    public void loadChunkListForTileEntity(ResourceLocation name, boolean update) {
        if (!update) setReloadListener(DataRequestType.TILEENTITYDATA, () -> loadChunkListForTileEntity(name, true));
        TileEntityData data = DataHolder.getLatestTileEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptionsOfType(name);
        list.forEach(option -> {
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to the center of the chunk", x, y, parent),
                (x) -> WorldUtils.teleportPlayer(Minecraft.getInstance().player, Minecraft.getInstance().player.getEntityWorld(), Minecraft.getInstance().player.getEntityWorld(), WorldUtils.getChunkCenter(option.chunkPos)));
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeTileEntitiesInChunk(Minecraft.getInstance().world, option.chunkPos, option.rl, false));
            option.addButton("Remove Block",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Delete all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeTileEntitiesInChunk(Minecraft.getInstance().world, option.chunkPos, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadTileEntitiesInChunkList(option.chunkPos, name, false);
        setCurrentList(list, numberComparer, action, update);
    }

    public void loadTileEntitiesInChunkList(ChunkPos chunkPos, ResourceLocation name, boolean update) {
        if (!update)
            setReloadListener(DataRequestType.TILEENTITYDATA, () -> loadTileEntitiesInChunkList(chunkPos, name, true));
        TileEntityData data = DataHolder.getLatestTileEntityData();
        if (data == null) return;
        List<CUOption> list = data.getInChunkAsCUOptions(chunkPos, name);
        list.forEach(option -> {
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to Entity", x, y, parent),
                x -> tpAction.accept(option));
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove TileEntity", x, y, parent),
                x -> WorldUtils.removeTileEntity(Minecraft.getInstance().world, option.id, false));
            option.addButton("Remove Block",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Delete Block of this TileEntity", x, y, parent),
                x -> WorldUtils.removeTileEntity(Minecraft.getInstance().world, option.id, true));
        });
        setCurrentList(list, positionSorter, tpAction, update);
    }

    public void loadStateList(boolean update) {
        if (!update) setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadStateList(true));
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getStatesAsDropdownOptions("");
        Consumer<CUOption> action = option -> loadFilteredStateList(option.getString(), false);
        setCurrentList(list, null, action, update);
    }

    private void loadFilteredStateList(String filter, boolean update) {
        if (!update) setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadFilteredStateList(filter, true));
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getStatesAsDropdownOptions(filter);
        list.forEach(option ->
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to the center of the chunk", x, y, parent),
                (x) -> WorldUtils.teleportPlayer(Minecraft.getInstance().player, Minecraft.getInstance().player.getEntityWorld(), Minecraft.getInstance().player.getEntityWorld(), WorldUtils.getChunkCenter(option.chunkPos))));
        setCurrentList(list, null, null, update);
    }

    public void loadTicketList(boolean update) {
        if (!update) setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadTicketList(true));
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getTicketsAsDropdownOptions("");
        Consumer<CUOption> action = option -> loadFilteredTicketList(option.getString(), false);
        setCurrentList(list, null, action, update);
    }

    private void loadFilteredTicketList(String filter, boolean update) {
        if (!update) setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadFilteredTicketList(filter, true));
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getTicketsAsDropdownOptions(filter);
        list.forEach(option ->
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to the center of the chunk", x, y, parent),
                (x) -> WorldUtils.teleportPlayer(Minecraft.getInstance().player, Minecraft.getInstance().player.getEntityWorld(), Minecraft.getInstance().player.getEntityWorld(), WorldUtils.getChunkCenter(option.chunkPos))));
        setCurrentList(list, null, null, update);
    }

    public void loadPlayerList(boolean update) {
        if (!update) setReloadListener(DataRequestType.PLAYERDATA, () -> loadPlayerList(true));
        PlayerData data = DataHolder.getLatestPlayerData();
        if (data == null) return;
        List<CUOption> list = data.getCUPlayers(Minecraft.getInstance().player.getName().getString());
        list.forEach(option -> {
            option.addButton("Inventory", (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Open the Players Inventory", x, y, parent), x -> {
                if (option.getString() != null) {
                    Network.sendToServer(new PlayerInventoryRequestMessage(option.string));
                }
            });
            option.addButton("Teleport", (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to the player", x, y, parent), x -> {
                if (option.getString() != null) {
                    Network.sendToServer(new TeleportToPlayerMessage(option.getString()));
                }
            });
        });
        setCurrentList(list, stringSorter, null, update);
    }
}
