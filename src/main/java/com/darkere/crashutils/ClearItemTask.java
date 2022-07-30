package com.darkere.crashutils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.*;

public class ClearItemTask extends TimerTask {

    public static ClearItemTask INSTANCE;
    int maxItems;
    List<Integer> list = new ArrayList<>();
    Timer timer;
    public int lastCount;
    public static void start(){
        if(CrashUtils.SERVER_CONFIG.getEnabled()){
            INSTANCE = new ClearItemTask();
            INSTANCE.loadConfigsAndStart();
        }
    }
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

    private void runClear(ServerLevel world) {
        List<Entity> entityList = new ArrayList<>();
        world.getEntities().getAll().forEach(x->{
            if(x.getType().equals(EntityType.ITEM))
                entityList.add(x);

        });
        lastCount = entityList.size();
        if (lastCount < maxItems) return;
        String text = CrashUtils.SERVER_CONFIG.getWarningText();
        int last = list.get(list.size() - 1);
        for (Integer integer : list) {
            if (integer.equals(last)) {
                new java.util.Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        List<Entity> entityList = new ArrayList<>();
                        world.getEntities().getAll().forEach(x->{
                            if(x.getType().equals(EntityType.ITEM))
                                entityList.add(x);

                        });
                        int size = list.size();
                        if (size > maxItems) {
                            entityList.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
                            world.getServer().getPlayerList().broadcastSystemMessage(CommandUtils.CreateTextComponent(size + " Items cleared"),false);
                        } else {
                            world.getServer().getPlayerList().broadcastSystemMessage(CommandUtils.CreateTextComponent("Item Clear prevented. Only " + size + " items on the ground"),false);
                        }

                    }
                }, integer * 1000L);

            }
            String intText = text.replaceFirst("%", integer.toString());
            Component message = CommandUtils.CreateTextComponent("[=== ").append(CommandUtils.CreateTextComponent(intText).withStyle(ChatFormatting.RED)).append(CommandUtils.CreateTextComponent(" ===]"));
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    world.getServer().getPlayerList().broadcastSystemMessage(message, false);
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
