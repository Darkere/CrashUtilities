package com.darkere.crashutils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class ClearItemTask extends TimerTask {

    public static boolean scheduled = false;
    public boolean enabled;
    int maxItems;
    List<Integer> list;

    @Override
    public void run() {
        scheduled = true;
    }

    public void setup() {
        enabled = CrashUtils.SERVER_CONFIG.getEnabled();
        maxItems = CrashUtils.SERVER_CONFIG.getMaximum();
        list = CrashUtils.SERVER_CONFIG.getWarnings();
        list.sort(Comparator.comparing(Integer::intValue));
    }

    public void checkItemCounts(ServerWorld world) {
        if (scheduled && enabled) {
            scheduled = false;
            List<Entity> list = world.getEntities().filter(x -> x.getType().equals(EntityType.ITEM)).collect(Collectors.toList());
            if (list.size() > maxItems) {
                runClear(world);
            }
        }
    }


    private void runClear(ServerWorld world) {
        setup();
        String text = CrashUtils.SERVER_CONFIG.getWarningText();
        PlayerList playerList = world.getServer().getPlayerList();
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
                            world.getServer().getPlayerList().sendMessage(new StringTextComponent(size + " Items cleared"));
                        } else {
                            world.getServer().getPlayerList().sendMessage(new StringTextComponent("Item Clear prevented. Only " + size + " items on the ground"));
                        }

                    }
                }, integer * 1000);

            }
            String intText = text.replaceFirst("%", integer.toString());
            ITextComponent message = new StringTextComponent("[=== ").appendSibling(new StringTextComponent(intText).setStyle(new Style().setColor(TextFormatting.RED))).appendSibling(new StringTextComponent(" ===]"));
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    playerList.sendMessage(message);
                }
            }, (last - integer) * 1000);

        }


        if (!CrashUtils.SERVER_CONFIG.getTitle()) return;
        try {
            if (world.getServer().getPlayerList().getPlayers().size() == 0) return;
            world.getServer().getCommandManager().getDispatcher().execute("title @a title {\"text\":\"" + CrashUtils.SERVER_CONFIG.getTitleText() + "\",\"color\":\"dark_red\"}", world.getServer().getCommandSource());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

}
