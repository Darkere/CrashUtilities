package com.darkere.crashutils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class ClearItemTask extends TimerTask {

    public static ClearItemTask INSTANCE;
    public boolean enabled;
    int maxItems;
    List<Integer> list = new ArrayList<>();
    Timer timer;

    public static void restart() {
        if(INSTANCE != null){
            INSTANCE.shutdown();
        }
        if(CrashUtils.SERVER_CONFIG.getEnabled()){
            INSTANCE = new ClearItemTask();
            INSTANCE.loadConfigsAndStart();
        }
    }

    private void shutdown() {
        if(timer != null){
            timer.cancel();
        }
        list.clear();
        maxItems = 5000;
    }

    public void loadConfigsAndStart() {
        if(INSTANCE.timer != null){
            INSTANCE.timer.cancel();
        }
        INSTANCE.timer = new Timer("CU Clear Item Task",true);
        maxItems = CrashUtils.SERVER_CONFIG.getMaximum();
        list = CrashUtils.SERVER_CONFIG.getWarnings();
        list.sort(Comparator.comparing(Integer::intValue));
        int time = CrashUtils.SERVER_CONFIG.getTimer() * 60 * 1000;
        if(time == 0) time = 10000;
        if (CrashUtils.SERVER_CONFIG.getEnabled())
            timer.scheduleAtFixedRate(this, time, time);
    }

    @Override
    public void run() {
        CrashUtils.runNextTick(this::runClear);
    }

    private void runClear(ServerWorld world) {
        List<Entity> entityList = world.getEntities().filter(x -> x.getType().equals(EntityType.ITEM)).collect(Collectors.toList());
        if (entityList.size() < maxItems) return;
        String text = CrashUtils.SERVER_CONFIG.getWarningText();
        int last = list.get(list.size() - 1);
        for (Integer integer : list) {
            if (integer.equals(last)) {
                new java.util.Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        List<Entity> list = world.getEntities().filter(x -> x.getType().equals(EntityType.ITEM)).collect(Collectors.toList());
                        int size = list.size();
                        if (size > maxItems) {
                            list.forEach(Entity::remove);
                            world.getServer().getPlayerList().broadcastMessage(new StringTextComponent(size + " Items cleared"), ChatType.SYSTEM, Util.NIL_UUID);
                        } else {
                            world.getServer().getPlayerList().broadcastMessage(new StringTextComponent("Item Clear prevented. Only " + size + " items on the ground"),ChatType.SYSTEM, Util.NIL_UUID);
                        }

                    }
                }, integer * 1000L);

            }
            String intText = text.replaceFirst("%", integer.toString());
            ITextComponent message = new StringTextComponent("[=== ").append(new StringTextComponent(intText).withStyle(TextFormatting.RED)).append(new StringTextComponent(" ===]"));
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    world.getServer().getPlayerList().broadcastMessage(message, ChatType.SYSTEM,Util.NIL_UUID);
                }
            }, (last - integer) * 1000L);

        }


        if (!CrashUtils.SERVER_CONFIG.getTitle()) return;
        try {
            if (world.getServer().getPlayerList().getPlayers().size() == 0) return;
            world.getServer().getCommands().getDispatcher().execute("title @a title {\"text\":\"" + CrashUtils.SERVER_CONFIG.getTitleText() + "\",\"color\":\"dark_red\"}", world.getServer().createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

}
