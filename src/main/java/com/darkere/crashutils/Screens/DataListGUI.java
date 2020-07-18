package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.WorldPos;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.TeleportMessage;
import com.darkere.crashutils.Screens.Types.DropDownType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class DataListGUI extends CUContentPane {
    CUDropDown SELECTOR;
    CUDropDown ENTITIES;
    CUDropDown TILEENTITIES;
    CUDropDown ENTITYCHUNKLIST;
    CUDropDown TILEENTITYCHUNKLIST;
    CUDropDown TICKETS;
    CUDropDown TICKETCHUNKLIST;
    CUDropDown LOCATIONS;
    CUDropDown STATECHUNKLIST;
    CUDropDown TICKETCOUNTLIST;
    CUDropDown STATECOUNTLIST;

    DataListGUI(CUScreen screen, RegistryKey<World> dim) {
        super(dim, screen);
        DataHolder.requestUpdates(DataRequestType.ENTITYDATA, 0, dim, true);
        LOCATIONS = new CUDropDown(DropDownType.LOCATIONS, screen, DataHolder.getLatestChunkData() == null ? new ArrayList<>() : new ArrayList<>(DataHolder.getLatestChunkData().getChunksByLocationType().keySet()), "all", -100, -105, 0);
        TICKETS = new CUDropDown(DropDownType.TICKETS, screen, DataHolder.getLatestChunkData() == null ? new ArrayList<>() : new ArrayList<>(DataHolder.getLatestChunkData().getChunksByTicketName().keySet()), "all", -100, -105, 0);
        SELECTOR = new CUDropDown(DropDownType.SELECTOR, screen, Arrays.asList("ENTITIES", "TILEENTITIES", "TICKETS", "STATES"), "ENTITIES", -192, -105, 75);
        ENTITIES = new CUDropDown(DropDownType.ENTITIES, screen, DataHolder.getLatestEntityData() == null ? new ArrayList<>() : DataHolder.getLatestEntityData().getMap().entrySet().stream().filter(e -> e.getValue().size() > 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).map(e -> "[" + e.getValue().size() + "] " + e.getKey().toString()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
            Collections.reverse(l);
            return l;
        })), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        TILEENTITIES = new CUDropDown(DropDownType.TILEENTITIES, screen, DataHolder.getLatestTileEntityData() == null ? new ArrayList<>() : DataHolder.getLatestTileEntityData().getMap().entrySet().stream().filter(e -> e.getValue().size() > 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).map(e -> "[" + e.getValue().size() + "] " + " " + e.getKey().toString()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
            Collections.reverse(l);
            return l;
        })), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        ENTITYCHUNKLIST = new CUDropDown(DropDownType.ENTITYCHUNKLIST, screen, DataHolder.getLatestEntityData() == null ? new ArrayList<>() : DataHolder.getLatestEntityData().getChunkMap().entrySet().stream().filter(e -> e.getValue() > 0).sorted(Map.Entry.comparingByValue()).map(e -> "[" + e.getValue() + "] " + " " + e.getKey()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
            Collections.reverse(l);
            return l;
        })), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        TILEENTITYCHUNKLIST = new CUDropDown(DropDownType.TILEENTITYCHUNKLIST, screen, DataHolder.getLatestTileEntityData() == null ? new ArrayList<>() : DataHolder.getLatestTileEntityData().getChunkMap().entrySet().stream().filter(e -> e.getValue() > 0).sorted(Map.Entry.comparingByValue()).map(e -> "[" + e.getValue() + "] " + e.getKey()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
            Collections.reverse(l);
            return l;
        })), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        TICKETCHUNKLIST = new CUDropDown(DropDownType.TICKETLIST, screen, DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getTicketsByChunk().entrySet().stream().map(e -> {
            StringBuilder s = new StringBuilder();
            s.append(e.getKey().toString()).append(" ");
            e.getValue().forEach(str -> s.append(str).append(", "));
            s.deleteCharAt(s.length() - 1);
            s.deleteCharAt(s.length() - 1);
            return s.toString();
        }).collect(Collectors.toList()), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        STATECOUNTLIST = new CUDropDown(DropDownType.STATECOUNTLIST, screen, DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getChunksByLocationType().entrySet().stream().map(e -> "[" + e.getValue().size() + "] " + e.getKey()).collect(Collectors.toList()), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        TICKETCOUNTLIST = new CUDropDown(DropDownType.TICKETCOUNTLIST, screen, DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getChunksByTicketName().entrySet().stream().map(e -> "[" + e.getValue().size() + "] " + e.getKey()).collect(Collectors.toList()), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        STATECHUNKLIST = new CUDropDown(DropDownType.LOCATIONCHUNKLIST, screen, DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getLocationTypeByChunk().entrySet().stream().map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.toList()), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        ENTITIES.setAlwaysExpanded();
        ENTITIES.setFitOnScreen(16);
        ENTITIES.setSortByName(false);
        TILEENTITIES.setAlwaysExpanded();
        TILEENTITIES.setFitOnScreen(16);
        TILEENTITIES.setSortByName(false);
        ENTITYCHUNKLIST.setAlwaysExpanded();
        ENTITYCHUNKLIST.setFitOnScreen(16);
        ENTITYCHUNKLIST.setSortByName(false);
        TILEENTITYCHUNKLIST.setAlwaysExpanded();
        TILEENTITYCHUNKLIST.setFitOnScreen(16);
        TILEENTITYCHUNKLIST.setSortByName(false);
        TICKETCHUNKLIST.setAlwaysExpanded();
        TICKETCHUNKLIST.setFitOnScreen(16);
        TICKETCHUNKLIST.setSortByName(false);
        STATECHUNKLIST.setAlwaysExpanded();
        STATECHUNKLIST.setFitOnScreen(16);
        STATECHUNKLIST.setSortByName(false);
        TICKETCOUNTLIST.setAlwaysExpanded();
        TICKETCOUNTLIST.setFitOnScreen(16);
        TICKETCOUNTLIST.setSortByName(false);
        STATECOUNTLIST.setAlwaysExpanded();
        STATECOUNTLIST.setFitOnScreen(16);
        STATECOUNTLIST.setSortByName(false);
        screen.dropDowns.clear();
        screen.dropDowns.add(ENTITIES);
        screen.dropDowns.add(TILEENTITIES);
        screen.dropDowns.add(ENTITYCHUNKLIST);
        screen.dropDowns.add(TILEENTITYCHUNKLIST);
        screen.dropDowns.add(TICKETCHUNKLIST);
        screen.dropDowns.add(STATECHUNKLIST);
        screen.dropDowns.add(TICKETCOUNTLIST);
        screen.dropDowns.add(STATECOUNTLIST);
        screen.topDropDowns.add(SELECTOR);
        screen.topDropDowns.add(LOCATIONS);
        screen.topDropDowns.add(TICKETS);
        SELECTOR.setEnabled(true);
        ENTITIES.setEnabled(true);
        DataHolder.registerListener(() -> {
            ENTITIES.updateOptions(DataHolder.getLatestEntityData() == null ? new ArrayList<>() : DataHolder.getLatestEntityData().getMap().entrySet().stream().filter(e -> e.getValue().size() > 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).map(e -> "[" + e.getValue().size() + "] " + e.getKey().toString()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
                Collections.reverse(l);
                return l;
            })));
            TILEENTITIES.updateOptions(DataHolder.getLatestTileEntityData() == null ? new ArrayList<>() : DataHolder.getLatestTileEntityData().getMap().entrySet().stream().filter(e -> e.getValue().size() > 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).map(e -> "[" + e.getValue().size() + "] " + e.getKey().toString()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
                Collections.reverse(l);
                return l;
            })));
            LOCATIONS.updateOptions(DataHolder.getLatestChunkData() == null ? new ArrayList<>() : new ArrayList<>(DataHolder.getLatestChunkData().getChunksByLocationType().keySet()));
            TICKETS.updateOptions(DataHolder.getLatestChunkData() == null ? new ArrayList<>() : new ArrayList<>(DataHolder.getLatestChunkData().getChunksByTicketName().keySet()));
            ENTITYCHUNKLIST.updateOptions(DataHolder.getLatestEntityData() == null ? new ArrayList<>() : DataHolder.getLatestEntityData().getChunkMap().entrySet().stream().filter(e -> e.getValue() > 0).sorted(Map.Entry.comparingByValue()).map(e -> "[" + e.getValue() + "] " + " " + e.getKey()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
                Collections.reverse(l);
                return l;
            })));
            TILEENTITYCHUNKLIST.updateOptions(DataHolder.getLatestTileEntityData() == null ? new ArrayList<>() : DataHolder.getLatestTileEntityData().getChunkMap().entrySet().stream().filter(e -> e.getValue() > 0).sorted(Map.Entry.comparingByValue()).map(e -> "[" + e.getValue() + "] " + " " + e.getKey()).collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
                Collections.reverse(l);
                return l;
            })));
            TICKETCHUNKLIST.updateOptions(DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getTicketsByChunk().entrySet().stream().map(e -> {
                StringBuilder s = new StringBuilder();
                s.append(e.getKey().toString()).append(" ");
                e.getValue().forEach(str -> s.append(str).append(", "));
                s.deleteCharAt(s.length() - 1);
                s.deleteCharAt(s.length() - 1);
                return s.toString();
            }).collect(Collectors.toList()));
            TICKETCOUNTLIST.updateOptions(DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getChunksByTicketName().entrySet().stream().map(e -> "[" + e.getValue().size() + "] " + e.getKey()).collect(Collectors.toList()));
            STATECOUNTLIST.updateOptions(DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getChunksByLocationType().entrySet().stream().map(e -> "[" + e.getValue().size() + "] " + e.getKey()).collect(Collectors.toList()));
            STATECHUNKLIST.updateOptions(DataHolder.getLatestChunkData() == null ? new ArrayList<>() : DataHolder.getLatestChunkData().getLocationTypeByChunk().entrySet().stream().map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.toList()));
        });
    }

    @Override
    public void updateSelection(DropDownType ddtype, String s) {

        if (s.equals("all")) s = "";
        switch (ddtype) {
            case SELECTOR:
                screen.dropDowns.forEach(x -> x.setEnabled(false));
                screen.topDropDowns.forEach(x -> x.setEnabled(false));
                SELECTOR.setEnabled(true);
                switch (s) {
                    case "ENTITIES":
                        DataHolder.requestUpdates(DataRequestType.ENTITYDATA, 0, screen.dim, !firstEntity);
                        currentType = DataRequestType.ENTITYDATA;
                        firstEntity = true;
                        ENTITIES.setEnabled(true);
                        break;
                    case "TILEENTITIES":
                        DataHolder.requestUpdates(DataRequestType.TILEENTITYDATA, 0, screen.dim, !firstTileEntity);
                        currentType = DataRequestType.TILEENTITYDATA;
                        firstTileEntity = true;
                        TILEENTITIES.setEnabled(true);
                        break;
                    case "TICKETS":
                        TICKETS.setEnabled(true);
                        DataHolder.requestUpdates(DataRequestType.LOADEDCHUNKDATA, 0, screen.dim, !firstChunks);
                        firstChunks = true;
                        TICKETCOUNTLIST.setEnabled(true);
                        break;
                    case "STATES":
                        LOCATIONS.setEnabled(true);
                        DataHolder.requestUpdates(DataRequestType.LOADEDCHUNKDATA, 0, screen.dim, !firstChunks);
                        firstChunks = true;
                        STATECOUNTLIST.setEnabled(true);
                        break;
                }
                break;
            case TICKETS:
                TICKETCOUNTLIST.setEnabled(false);
                DataHolder.setChunkDataFilter(s);
                DataHolder.requestUpdates(DataRequestType.LOADEDCHUNKDATA, 0, screen.dim, true);
                DataHolder.notifyListener();
                TICKETCHUNKLIST.setEnabled(true);
                break;
            case TICKETCOUNTLIST:
                TICKETCOUNTLIST.setEnabled(false);
                DataHolder.setChunkDataFilter(getStringWithoutPrefixNumbers(s));
                DataHolder.requestUpdates(DataRequestType.LOADEDCHUNKDATA, 0, screen.dim, true);
                DataHolder.notifyListener();
                TICKETCHUNKLIST.setEnabled(true);
                break;
            case STATECOUNTLIST:
                STATECOUNTLIST.setEnabled(false);
                DataHolder.setChunkDataFilter(getStringWithoutPrefixNumbers(s));
                DataHolder.requestUpdates(DataRequestType.LOADEDCHUNKDATA, 0, screen.dim, true);
                DataHolder.notifyListener();
                STATECHUNKLIST.setEnabled(true);
                break;
            case LOCATIONS:
                STATECOUNTLIST.setEnabled(false);
                DataHolder.setChunkDataFilter(s);
                DataHolder.requestUpdates(DataRequestType.LOADEDCHUNKDATA, 0, screen.dim, true);
                DataHolder.notifyListener();
                STATECHUNKLIST.setEnabled(true);
                break;
            case ENTITIES:
                ENTITIES.setEnabled(false);
                DataHolder.setEntityFilter(getStringWithoutPrefixNumbers(s));
                ENTITYCHUNKLIST.setEnabled(true);
                DataHolder.notifyListener();
                break;
            case TILEENTITIES:
                TILEENTITIES.setEnabled(false);
                DataHolder.setTileEntityFilter(getStringWithoutPrefixNumbers(s));
                TILEENTITYCHUNKLIST.setEnabled(true);
                DataHolder.notifyListener();
                break;
            case ENTITYCHUNKLIST:
                tpToWorldPos(DataHolder.getLatestEntityData().getTpforChunk(getChunkfromString(getStringWithoutPrefixNumbers(s))));
                break;
            case TILEENTITYCHUNKLIST:
                tpToWorldPos(DataHolder.getLatestTileEntityData().getTpforChunk(getChunkfromString(getStringWithoutPrefixNumbers(s))));
                break;
            case TICKETLIST:
            case LOCATIONCHUNKLIST:
                tpToStringChunk(s);
                break;
        }
    }

    private String getStringWithoutPrefixNumbers(String s) {
        return (s == null) ? null : s.substring(s.indexOf("]") + 2);
    }

    private ChunkPos getChunkfromString(String s) {
        if (s == null) return null;
        s = s.trim();
        String x = s.substring(1, s.indexOf(","));
        String y = s.substring(s.indexOf(",") + 2, s.indexOf("]"));
        return new ChunkPos(Integer.parseInt(x), Integer.parseInt(y));
    }

    private void tpToStringChunk(String s) {
        if (s != null && !s.isEmpty())
            Network.sendToServer(new TeleportMessage(dim, dim, getChunkfromString(s).asBlockPos()));
    }

    private void tpToWorldPos(WorldPos pos) {
        if (pos != null)
            Network.sendToServer(new TeleportMessage(dim, dim, pos.pos));

    }

    public void addToToolTip(IFormattableTextComponent tooltips, int mx, int my) {
        if (ENTITIES.isEnabled() && ENTITIES.isMouseOver(mx, my)) {
            tooltips.func_230529_a_(new StringTextComponent("Click to see Chunk Counts for this Entity"));
        } else if (TILEENTITIES.isEnabled() && TILEENTITIES.isMouseOver(mx, my)) {
            tooltips.func_230529_a_(new StringTextComponent("Click to see Chunk Counts for this Tileentity"));
        } else if (TICKETCHUNKLIST.isEnabled() && TICKETCHUNKLIST.isMouseOver(mx, my) || STATECHUNKLIST.isEnabled() && STATECHUNKLIST.isMouseOver(mx, my)) {
            tooltips.func_230529_a_(new StringTextComponent("Click to teleport to the specified Chunk"));
        } else if (ENTITYCHUNKLIST.isEnabled() && ENTITYCHUNKLIST.isMouseOver(mx, my)) {
            tooltips.func_230529_a_(new StringTextComponent("Click to teleport to one of the Entities in the specified Chunk"));
        } else if (TILEENTITYCHUNKLIST.isEnabled() && TILEENTITYCHUNKLIST.isMouseOver(mx, my)) {
            tooltips.func_230529_a_(new StringTextComponent("Click to teleport to one of the Tileentities in the specified Chunk"));
        }

    }
}

