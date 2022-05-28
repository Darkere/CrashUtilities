package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.*;
import com.darkere.crashutils.Network.*;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

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
    ResourceKey<Level> world;
    Consumer<CUOption> tpAction = option -> {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        WorldUtils.teleportPlayer(player, player.getCommandSenderWorld(), player.getCommandSenderWorld(), option.blockPos);
    };
    List<Consumer<Boolean>> history = new ArrayList<>();

    public DataListLoader(int XTopLeft, int YTopLeft, int XAcross, int YAcross, CUScreen screen, ResourceKey<Level> world) {
        this.XTopLeft = XTopLeft;
        this.YTopLeft = YTopLeft;
        this.XAcross = XAcross;
        this.YAcross = YAcross;
        parent = screen;
        this.world = world;
        setCurrentList(new ArrayList<>(), null, null, false);
    }

    public void goBack() {
        if (history.size() > 0)
            history.remove(history.size() - 1);
        if (history.size() > 0)
            history.get(history.size() - 1).accept(true);
        else
            loadOrderedEntityList(true);
    }
    
    private void addToHistory(Consumer<Boolean> runnable){
        history.add(runnable);
    }

    private void setCurrentList(List<CUOption> list, Consumer<List<CUOption>> sorter, Consumer<CUOption> action, boolean isUpdate) {
        if (isUpdate) {
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

    public void loadOrderedEntityList(boolean isUpdate) {

        if (!isUpdate) {
            setReloadListener(DataRequestType.ENTITYDATA, () -> loadOrderedEntityList(true));
            addToHistory(this::loadOrderedEntityList);
        }

        EntityData data = DataHolder.getLatestEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptions();
        list.forEach(option -> {
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all entities of this type", x, y, parent),
                x -> WorldUtils.removeEntityType(Minecraft.getInstance().level, option.rl, false));
            option.addButton("Wipe",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Forcefully remove all entities of this type", x, y, parent),
                x -> WorldUtils.removeEntityType(Minecraft.getInstance().level, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadChunkListForEntity(option.getRl(), false);
        setCurrentList(list, numberComparer, action, isUpdate);
    }

    public void loadChunkListForEntity(ResourceLocation name, boolean isUpdate) {

        if (!isUpdate){
            setReloadListener(DataRequestType.ENTITYDATA, () -> loadChunkListForEntity(name, true));
            addToHistory((newUpdate) -> loadChunkListForEntity(name, newUpdate));
        }
        EntityData data = DataHolder.getLatestEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptionsOfType(name);
        list.forEach(option -> {
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeEntitiesInChunk(Minecraft.getInstance().level, option.chunkPos, option.rl, false));
            option.addButton("Wipe",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Forcefully remove all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeEntitiesInChunk(Minecraft.getInstance().level, option.chunkPos, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadEntitiesInChunkAsList(option.chunkPos, name, false);
        setCurrentList(list, numberComparer, action, isUpdate);
    }

    public void loadEntitiesInChunkAsList(ChunkPos chunkPos, ResourceLocation name, boolean isUpdate) {
        if (!isUpdate){
            setReloadListener(DataRequestType.ENTITYDATA, () -> loadEntitiesInChunkAsList(chunkPos, name, true));
            addToHistory((newUpdate) -> loadEntitiesInChunkAsList(chunkPos, name, newUpdate));
        }

        EntityData data = DataHolder.getLatestEntityData();
        if (data == null) return;
        List<CUOption> list = data.getInChunkAsCUOptions(chunkPos, name);
        list.forEach(option -> {
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to Entity", x, y, parent),
                x -> tpAction.accept(option));
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove this Entity", x, y, parent),
                x -> WorldUtils.removeEntity(Minecraft.getInstance().level, option.id, false));
            option.addButton("Wipe",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Forcefully remove this Entity", x, y, parent),
                x -> WorldUtils.removeEntity(Minecraft.getInstance().level, option.id, true));
        });
        setCurrentList(list, positionSorter, null, isUpdate);
    }

    public void loadOrderedTileEntityList(boolean isUpdate) {
        if (!isUpdate){
            setReloadListener(DataRequestType.TILEENTITYDATA, () -> loadOrderedTileEntityList(true));
            addToHistory(this::loadOrderedTileEntityList);
        }
        TileEntityData data = DataHolder.getLatestTileEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptions();
        list.forEach(option -> {
            option.addButton("Remove TE",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all loaded TileEntities of this type", x, y, parent),
                b -> WorldUtils.removeTileEntityType(Minecraft.getInstance().level, option.rl, false));
            option.addButton("Remove Block", (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Delete Blocks with this loaded TileEntity", x, y, parent),
                b -> WorldUtils.removeTileEntityType(Minecraft.getInstance().level, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadChunkListForTileEntity(option.rl, false);
        setCurrentList(list, numberComparer, action, isUpdate);
    }

    public void loadChunkListForTileEntity(ResourceLocation name, boolean isUpdate) {
        if (!isUpdate){
            setReloadListener(DataRequestType.TILEENTITYDATA, () -> loadChunkListForTileEntity(name, true));
            addToHistory((newUpdate) -> loadChunkListForTileEntity(name, newUpdate));
        }
        TileEntityData data = DataHolder.getLatestTileEntityData();
        if (data == null) return;
        List<CUOption> list = data.getAsCUOptionsOfType(name);
        list.forEach(option -> {
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to the center of the chunk", x, y, parent),
                (x) -> WorldUtils.teleportPlayer(Minecraft.getInstance().player, Minecraft.getInstance().player.getCommandSenderWorld(), Minecraft.getInstance().player.getCommandSenderWorld(), WorldUtils.getChunkCenter(option.chunkPos)));
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeTileEntitiesInChunk(Minecraft.getInstance().level, option.chunkPos, option.rl, false));
            option.addButton("Remove Block",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Delete all " + option.rl + " in this chunk", x, y, parent),
                x -> WorldUtils.removeTileEntitiesInChunk(Minecraft.getInstance().level, option.chunkPos, option.rl, true));
        });
        Consumer<CUOption> action = option -> loadTileEntitiesInChunkList(option.chunkPos, name, false);
        setCurrentList(list, numberComparer, action, isUpdate);
    }

    public void loadTileEntitiesInChunkList(ChunkPos chunkPos, ResourceLocation name, boolean isUpdate) {

        if (!isUpdate){
            setReloadListener(DataRequestType.TILEENTITYDATA, () -> loadTileEntitiesInChunkList(chunkPos, name, true));
            addToHistory((newUpdate) -> loadTileEntitiesInChunkList(chunkPos, name, newUpdate));
        }

        TileEntityData data = DataHolder.getLatestTileEntityData();
        if (data == null) return;
        List<CUOption> list = data.getInChunkAsCUOptions(chunkPos, name);
        list.forEach(option -> {
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to Entity", x, y, parent),
                x -> tpAction.accept(option));
            option.addButton("Remove",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Remove TileEntity", x, y, parent),
                x -> WorldUtils.removeTileEntity(Minecraft.getInstance().level, option.id, false));
            option.addButton("Remove Block",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Delete Block of this TileEntity", x, y, parent),
                x -> WorldUtils.removeTileEntity(Minecraft.getInstance().level, option.id, true));
        });
        setCurrentList(list, positionSorter, tpAction, isUpdate);
    }

    public void loadStateList(boolean isUpdate) {

        if (!isUpdate){
            setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadStateList(true));
            addToHistory(this::loadStateList);
        }
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getStatesAsDropdownOptions("");
        Consumer<CUOption> action = option -> loadFilteredStateList(option.getString(), false);
        setCurrentList(list, null, action, isUpdate);
    }

    private void loadFilteredStateList(String filter, boolean isUpdate) {

        if (!isUpdate){
            setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadFilteredStateList(filter, true));
            addToHistory((newUpdate) -> loadFilteredStateList(filter, newUpdate));
        }
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getStatesAsDropdownOptions(filter);
        list.forEach(option ->
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to the center of the chunk", x, y, parent),
                (x) -> WorldUtils.teleportPlayer(Minecraft.getInstance().player, Minecraft.getInstance().player.getCommandSenderWorld(), Minecraft.getInstance().player.getCommandSenderWorld(), WorldUtils.getChunkCenter(option.chunkPos))));
        setCurrentList(list, null, null, isUpdate);
    }

    public void loadTicketList(boolean isUpdate) {

        if (!isUpdate){
            setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadTicketList(true));
            addToHistory((newUpdate) -> loadTicketList(newUpdate));
        }
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getTicketsAsDropdownOptions("");
        Consumer<CUOption> action = option -> loadFilteredTicketList(option.getString(), false);
        setCurrentList(list, null, action, isUpdate);
    }

    private void loadFilteredTicketList(String filter, boolean isUpdate) {

        if (!isUpdate){
            setReloadListener(DataRequestType.LOADEDCHUNKDATA, () -> loadFilteredTicketList(filter, true));
            addToHistory((newUpdate) -> loadFilteredTicketList(filter, newUpdate));
        }
        LoadedChunkData data = DataHolder.getLatestChunkData();
        if (data == null) return;
        List<CUOption> list = data.getTicketsAsDropdownOptions(filter);
        list.forEach(option ->
            option.addButton("Teleport",
                (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Teleport to the center of the chunk", x, y, parent),
                (x) -> WorldUtils.teleportPlayer(Minecraft.getInstance().player, Minecraft.getInstance().player.getCommandSenderWorld(), Minecraft.getInstance().player.getCommandSenderWorld(), WorldUtils.getChunkCenter(option.chunkPos))));
        setCurrentList(list, null, null, isUpdate);
    }

    public void loadPlayerList(boolean isUpdate) {

        if (!isUpdate){
            setReloadListener(DataRequestType.PLAYERDATA, () -> loadPlayerList(true));
            addToHistory(this::loadPlayerList);
        }
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
            option.addButton("EnderChest", (button, stack, x, y) -> GuiTools.drawTextToolTip(stack, "Open Enderchest", x, y, parent), x -> {
                if (option.getString() != null) {
                    Network.sendToServer(new OpenEnderChestMessage(option.string));
                }
            });
        });
        setCurrentList(list, stringSorter, null, isUpdate);
    }
}
