package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Screens.Types.DropDownType;
import com.darkere.crashutils.Screens.Types.GridRenderType;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class GridChunkGUI extends CUContentPane {
    int XOffset = -50;
    int YOffset = -50;
    Map<String, Integer> colormap = new HashMap<>();
    Random random = new Random();
    float zoom = 1;
    GridRenderType type = GridRenderType.LOCATIONTYPE;
    String renderFilter = null;
    CUDropDown RENDERTYPES;
    CUDropDown TICKETS;
    CUDropDown ENTITIES;
    CUDropDown TILEENTITIES;
    int hoveringX, hoveringY = 0;
    long blinkTime = 0;

    GridChunkGUI(CUScreen screen, DimensionType dim, BlockPos initial) {
        super(dim, screen);
        goTo(initial);
        DataHolder.requestUpdates(DataRequestType.LOADEDCHUNKDATA, 0, dim, true);
        colormap.put(null, 0x686868 + 0xFF000000); // GRAY
        colormap.put("PARTIALLYGENERATED", 0x66ff99 + 0xff000000); //GREEN
        colormap.put("BORDER", 0xffff99 + 0xff000000); //YELLOW
        colormap.put("PRIMED", 0x6600ff + 0xff000000); //BLUE
        colormap.put("ENTITY_TICKING", 0xff0000 + 0xff000000); //RED
        colormap.put("TICKING", 0xff00ff + 0xff000000); //PINK
        colormap.put("INACCESSIBLE", 0xff686868); //BLACK
        colormap.put("FULL", 0xff686868); //BLACK
        colormap.put("no_ticket", -6250336);//lighter Gray
        RENDERTYPES = new CUDropDown(DropDownType.RENDERTYPES, screen, Arrays.stream(GridRenderType.values()).map(x -> x.type).collect(Collectors.toList()), GridRenderType.LOCATIONTYPE.type, -192, -105, 75);
        TICKETS = new CUDropDown(DropDownType.TICKETS, screen, DataHolder.getLatestChunkData() == null ? new ArrayList<>() : new ArrayList<>(DataHolder.getLatestChunkData().getChunksByTicketName().keySet()), "all", -106, -105, 0);
        ENTITIES = new CUDropDown(DropDownType.ENTITIES, screen, DataHolder.getLatestEntityData() == null ? new ArrayList<>() : DataHolder.getLatestEntityData().getMap().keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList()), "all", -106, -105, 0);
        TILEENTITIES = new CUDropDown(DropDownType.TILEENTITIES, screen, DataHolder.getLatestTileEntityData() == null ? new ArrayList<>() : DataHolder.getLatestTileEntityData().getMap().keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList()), "all", -106, -105, 0);
        screen.topDropDowns.add(RENDERTYPES);
        screen.topDropDowns.add(TICKETS);
        screen.topDropDowns.add(ENTITIES);
        screen.topDropDowns.add(TILEENTITIES);
        RENDERTYPES.setEnabled(true);
        DataHolder.registerListener(() -> {
            TICKETS.updateOptions(DataHolder.getLatestChunkData() == null ? new ArrayList<>() : new ArrayList<>(DataHolder.getLatestChunkData().getChunksByTicketName().keySet()));
            ENTITIES.updateOptions(DataHolder.getLatestEntityData() == null ? new ArrayList<>() : DataHolder.getLatestEntityData().getMap().keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList()));
            TILEENTITIES.updateOptions(DataHolder.getLatestTileEntityData() == null ? new ArrayList<>() : DataHolder.getLatestTileEntityData().getMap().keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList()));
        });
    }

    public void render(int centerX, int centerY) {
        super.render(centerX, centerY);
        List<FillMany.ColoredRectangle> list = new ArrayList<>();
        for (int i = 0; i < XAcross; i++) {
            for (int j = 0; j < YAcross; j++) {
                int x = getColorForPixel(i, j);
                if (x == 0) continue;
                list.add(new FillMany.ColoredRectangle(i + XTopLeft, j + YTopLeft, i + 1 + XTopLeft, j + 1 + YTopLeft, x));
            }
        }
        FillMany.fillMany(TransformationMatrix.identity().getMatrix(), list);
    }
    private void goTo(BlockPos pos){
      ChunkPos chunkPos = new ChunkPos(pos);
        XOffset = -170 + chunkPos.x;
        YOffset = -90 + chunkPos.z;
    }

    public void setRenderType(GridRenderType type) {
        this.type = type;
    }

    public void setRenderFilter(String renderFilter) {
        this.renderFilter = renderFilter;
    }

    private int getColorForPixel(int i, int j) {
        int newi = Math.round(((float) i) / zoom);
        int newj = Math.round(((float) j) / zoom);
        int posX = newi + XOffset;
        int posY = newj + YOffset;
        if (posX == this.hoveringX && posY == this.hoveringY && System.currentTimeMillis() - blinkTime > 300) {
            if (System.currentTimeMillis() - blinkTime > 600) {
                blinkTime = System.currentTimeMillis();
            }
            return 0xFFFFFF;
        }
        String loc = null;
        int counts = 0;
        switch (type) {
            case LOCATIONTYPE:
                if (DataHolder.getLatestChunkData() == null) return -1;
                loc = DataHolder.getLatestChunkData().getLocationType(new ChunkPos(posX, posY));
                break;
            case TICKET:
                if (DataHolder.getLatestChunkData() == null) return -1;
                loc = DataHolder.getLatestChunkData().getTickets(new ChunkPos(posX, posY));
                break;
            case ENTITIES:
                if (DataHolder.getLatestEntityData() == null) return -1;
                counts = DataHolder.getLatestEntityData().getEntityCountForChunk(new ChunkPos(posX, posY));
                break;
            case TILEENTITIES:
                if (DataHolder.getLatestTileEntityData() == null) return -1;
                counts = DataHolder.getLatestTileEntityData().getTileEntityCountForChunk(new ChunkPos(posX, posY));
                break;

        }
        if (counts != 0) {
            float hue = 0;
            if (counts > 100) {
                hue = 0.4f;
            } else {
                hue = (1 - (counts / 100f)) * 0.4f;
            }
            final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
            final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
            return Color.getHSBColor(hue, saturation, luminance).getRGB() + 0xFF000000;
        }
        if (loc == null || loc.isEmpty() || loc.equals("[]")) return 0;
        if (renderFilter != null && !renderFilter.isEmpty()) {
            if (!loc.equals(renderFilter)) return 0;
        }
        if (colormap.containsKey(loc)) {
            return colormap.get(loc);
        } else {
            final float hue = random.nextFloat();
            final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
            final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
            int color = Color.getHSBColor(hue, saturation, luminance).getRGB() + 0xFF000000;
            colormap.put(loc, color);
            return color;
        }
    }

    @Override
    public void addOffset(double x, double y) {
        XOffset += Math.round(x / zoom);
        YOffset += Math.round(y / zoom);

    }

    @Override
    public void zoom(double x, double y, double delta, int centerX, int centerY) {
        double distX = x - (centerX + defaultRenderOffsetX);
        double distY = y - (centerY + defaultRenderOffsetY);
        distX /= zoom;
        distY /= zoom;

        if (delta > 0) {
            zoom *= 2;
            XOffset += distX / 2;
            YOffset += distY / 2;

        } else {
            zoom /= 2;
            XOffset -= distX ;
            YOffset -= distY ;

        }
    }

    public String getLocFor(int x, int y) {
        ChunkPos pos = getChunkFor(x, y);
        if (DataHolder.getLatestChunkData() == null) return null;
        return DataHolder.getLatestChunkData().getLocationType(pos);
    }

    public String getTicketsFor(int x, int y) {
        ChunkPos pos = getChunkFor(x, y);
        if (DataHolder.getLatestChunkData() == null) return null;
        return DataHolder.getLatestChunkData().getTickets(pos);
    }

    public String getEntityCountFor(int x, int y) {
        ChunkPos pos = getChunkFor(x, y);
        if (DataHolder.getLatestEntityData() == null) return null;
        return String.valueOf(DataHolder.getLatestEntityData().getEntityCountForChunk(pos));
    }

    public String getTileEntityCountFor(int x, int y) {
        ChunkPos pos = getChunkFor(x, y);
        if (DataHolder.getLatestTileEntityData() == null) return null;
        return String.valueOf(DataHolder.getLatestTileEntityData().getTileEntityCountForChunk(pos));
    }

    public ChunkPos getChunkFor(int x, int y) {
        double xPos = x - XTopLeft + (XOffset * zoom);
        double yPos = y - YTopLeft + (YOffset * zoom);
        int xiPos = (int) Math.round(xPos / zoom);
        int yiPos = (int) Math.round(yPos / zoom);
        this.hoveringX = xiPos;
        this.hoveringY = yiPos;
        return new ChunkPos(xiPos, yiPos);
    }

    public String getNameForLocationType(String s) {
        if (s == null) return "Nothing yet";
        switch (s) {
            case "ENTITY_TICKING":
                return "Fully loaded and ticking";
            case "TICKING":
                return "Loaded but not ticking entities";
            case "BORDER":
            case "INACCESSIBLE":
            case "FULL":
                return "Loaded but not ticking";
            case "PRIMED":
                return "Prepared for loading";
            case "PARTIALLYGENERATED":
                return "Partially generated";
        }
        return "???";
    }


    public void updateSelection(DropDownType ddtype, String s) {
        if (s.equals("all")) s = "";
        switch (ddtype) {
            case RENDERTYPES:
                GridRenderType type = GridRenderType.getTypeByName(s);
                if (type == null) return;
                setRenderType(type);
                setRenderFilter(null);
                screen.topDropDowns.forEach(x -> x.setEnabled(false));
                RENDERTYPES.setEnabled(true);
                switch (type) {
                    case LOCATIONTYPE:
                        currentType = DataRequestType.LOADEDCHUNKDATA;
                        break;
                    case TICKET:
                        TICKETS.setEnabled(true);
                        currentType = DataRequestType.LOADEDCHUNKDATA;
                        break;
                    case ENTITIES:
                        DataHolder.requestUpdates(DataRequestType.ENTITYDATA, 0, screen.dim, !firstEntity);
                        currentType = DataRequestType.ENTITYDATA;
                        firstEntity = true;
                        ENTITIES.setEnabled(true);
                        break;
                    case TILEENTITIES:
                        DataHolder.requestUpdates(DataRequestType.TILEENTITYDATA, 0, screen.dim, !firstTileEntity);
                        currentType = DataRequestType.TILEENTITYDATA;
                        firstTileEntity = true;
                        TILEENTITIES.setEnabled(true);
                        break;
                }
                break;
            case TICKETS:
                setRenderFilter(s);
                break;
            case ENTITIES:
                DataHolder.setEntityFilter(s);
                break;
            case TILEENTITIES:
                DataHolder.setTileEntityFilter(s);
                break;
        }
    }

}
