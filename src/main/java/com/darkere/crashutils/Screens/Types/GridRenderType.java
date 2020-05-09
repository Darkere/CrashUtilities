package com.darkere.crashutils.Screens.Types;

public enum GridRenderType{
    LOCATIONTYPE("Chunk Status"),
    TICKET("Chunk Ticket"),
    ENTITIES("Entities"),
    TILEENTITIES("Tile Entites");
    public String type;
    GridRenderType(String type){
        this.type = type;
    }
    public static GridRenderType getTypeByName(String s){
        switch (s){
            case "Chunk Status": return LOCATIONTYPE;
            case "Chunk Ticket": return TICKET;
            case "Entities": return ENTITIES;
            case "Tile Entites": return TILEENTITIES;
            default: return null;
        }
    }
}
