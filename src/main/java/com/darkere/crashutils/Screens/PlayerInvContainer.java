package com.darkere.crashutils.Screens;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class PlayerInvContainer extends AbstractContainerMenu {
    IItemHandler playerInventory;
    IItemHandler otherPlayerInventory;
    String otherPlayerName;
    Level world;
    Player player;
    Player otherPlayer;

    public Map<String, Integer> slotAmounts;


    public PlayerInvContainer(Player player, Player otherPlayer, int id, String otherPlayerName, Map<String, Integer> slotAmounts, int curioSlotcount) {
        super(null, id);
        this.otherPlayerName = otherPlayerName;
        this.world = player.getCommandSenderWorld();
        this.player = player;
        this.playerInventory = new InvWrapper(player.getInventory());
        if (otherPlayer == null) {
            SimpleContainer i = new SimpleContainer(41) {
                @Override
                public boolean canPlaceItem(int index, ItemStack stack) {
                    if (index == 36) {
                        return stack.canEquip(EquipmentSlot.FEET, player);
                    } else if (index == 37) {
                        return stack.canEquip(EquipmentSlot.LEGS, player);
                    } else if (index == 38) {
                        return stack.canEquip(EquipmentSlot.CHEST, player);
                    } else if (index == 39) {
                        return stack.canEquip(EquipmentSlot.HEAD, player);
                    }
                    return true;
                }
            };
            otherPlayerInventory = new InvWrapper(i);
        } else {
            this.otherPlayer = otherPlayer;
            otherPlayerInventory = new InvWrapper(otherPlayer.getInventory());
        }

        layoutPlayerInventorySlots(playerInventory, 25, 105);
        layoutPlayerInventorySlots(otherPlayerInventory, 25, -13);
        layoutArmorAndOffhandSlots(playerInventory, -10, 97);
        layoutArmorAndOffhandSlots(otherPlayerInventory, -10, -21);
//        if (CrashUtils.curiosLoaded) {
//            IItemHandler curiosInv = null;
//            if (slotAmounts != null) {
//                curiosInv = new InvWrapper(new Inventory(curioSlotcount));
//                this.slotAmounts = slotAmounts;
//            } else {
//                slotAmounts = new LinkedHashMap<>();
//                Map<String, Integer> finalSlotAmounts = slotAmounts;
//                CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(x -> x.getCurios().forEach((s, h) -> finalSlotAmounts.put(s, h.getSlots())));
//            }
//            layoutCurioSlots(otherPlayer, 204, -35, slotAmounts.values(), curiosInv);
//            layoutCurioSlots(player, 204, 85, slotAmounts.values(), null);
//        }

    }

    private void layoutArmorAndOffhandSlots(IItemHandler playerInventory, int x, int y) {
        y += 18 * 3;
        for (int i = 0; i < 4; i++) {
            int finalI = 36 + i;
            addSlot(new SlotItemHandler(playerInventory, finalI, x, y) {
                @Override
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    if (finalI == 36) {
                        return stack.canEquip(EquipmentSlot.FEET, player);
                    } else if (finalI == 37) {
                        return stack.canEquip(EquipmentSlot.LEGS, player);
                    } else if (finalI == 38) {
                        return stack.canEquip(EquipmentSlot.CHEST, player);
                    } else if (finalI == 39) {
                        return stack.canEquip(EquipmentSlot.HEAD, player);
                    }
                    return true;
                }
            });
            y -= 18;
        }
        addSlot(new SlotItemHandler(playerInventory, 40, x, y + 18 * 5 + 4));
    }


    private void layoutCurioSlots(Player player, int x, int y, Collection<Integer> curioSlots, IItemHandler curiosInv) {
        if (player != null) {
            Map<String, ICurioStacksHandler> curios = CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null).getCurios();
            if (curios == null) return;
            int temp = x;
            int g = 0;
            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                if (g == 4) {
                    y -= 120;
                }
                if (g < 4) {
                    x = temp;
                } else {
                    x = -26 - 18 * entry.getValue().getSlots();
                }
                addSlotRange(entry.getValue().getStacks(), 0, x, y, entry.getValue().getSlots(), 18);
                y += 30;
                g++;
            }
        } else {
            int temp = x;
            int g = 0;
            int index = 0;
            for (Integer j : curioSlots) {
                if (g == 4) {
                    y -= 120;
                }
                if (g < 4) {
                    x = temp;
                } else {
                    x = -26 - 18 * j;
                }
                addSlotRange(curiosInv, index, x, y, j, 18);
                index += j;
                y += 30;
                g++;
            }
        }

    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(IItemHandler playerInventory, int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);

        if (!world.isClientSide() && !playerIn.getServer().getPlayerList().getPlayers().contains(otherPlayer)) {
            ((ServerLevel) world).getServer().playerDataStorage.save(otherPlayer);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
