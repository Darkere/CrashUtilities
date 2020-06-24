package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CURegistry;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerInvContainer extends Container {
    IItemHandler playerInventory;
    IItemHandler otherPlayerInventory;
    String otherPlayerName;
    World world;
    PlayerEntity player;
    PlayerEntity otherPlayer;

    public Map<String, Integer> slotAmounts = new LinkedHashMap<>();

    public PlayerInvContainer(@Nullable ContainerType<?> type, PlayerEntity player, PlayerEntity otherPlayer, int id, PacketBuffer data) {
        super(type, id);
        world = player.getEntityWorld();
        this.player = player;
        this.playerInventory = new InvWrapper(player.inventory);
        if (otherPlayer == null) {
            Inventory i = new Inventory(41) {
                @Override
                public boolean isItemValidForSlot(int index, ItemStack stack) {
                    if (index == 36) {
                        return stack.canEquip(EquipmentSlotType.FEET, player);
                    } else if (index == 37) {
                        return stack.canEquip(EquipmentSlotType.LEGS, player);
                    } else if (index == 38) {
                        return stack.canEquip(EquipmentSlotType.CHEST, player);
                    } else if (index == 39) {
                        return stack.canEquip(EquipmentSlotType.HEAD, player);
                    }
                    return true;
                }
            };
            otherPlayerInventory = new InvWrapper(i);
            otherPlayerName = data.readString();
        } else {
            this.otherPlayer = otherPlayer;
            otherPlayerInventory = new InvWrapper(otherPlayer.inventory);
            otherPlayerName = otherPlayer.getName().getString();
        }

        layoutPlayerInventorySlots(playerInventory, 25, 105);
        layoutPlayerInventorySlots(otherPlayerInventory, 25, -13);
        layoutArmorAndOffhandSlots(playerInventory, -10, 97);
        layoutArmorAndOffhandSlots(otherPlayerInventory, -10, -21);
        if (CrashUtils.curiosLoaded) {
            IItemHandler curiosInv = null;
            if (data != null) {
                int size = data.readInt();
                for (int i = 0; i < size; i++) {
                    slotAmounts.put(data.readString(), data.readInt());
                }
                curiosInv = new InvWrapper(new Inventory(slotAmounts.values().stream().mapToInt(x -> x).sum()));
            } else {
                CuriosAPI.getCuriosHandler(player).ifPresent(x -> x.getCurioMap().forEach((s, h) -> slotAmounts.put(s, h.getSlots())));
            }
            layoutCurioSlots(otherPlayer, 204, -35, slotAmounts.values(), curiosInv);
            layoutCurioSlots(player,204,85,slotAmounts.values(),null);
        }

    }

    private void layoutArmorAndOffhandSlots(IItemHandler playerInventory, int x, int y) {
        y += 18 * 3;
        for (int i = 0; i < 4; i++) {
            int finalI = 36 + i;
            addSlot(new SlotItemHandler(playerInventory, finalI, x, y) {
                @Override
                public boolean isItemValid(@Nonnull ItemStack stack) {
                    if (finalI == 36) {
                        return stack.canEquip(EquipmentSlotType.FEET, player);
                    } else if (finalI == 37) {
                        return stack.canEquip(EquipmentSlotType.LEGS, player);
                    } else if (finalI == 38) {
                        return stack.canEquip(EquipmentSlotType.CHEST, player);
                    } else if (finalI == 39) {
                        return stack.canEquip(EquipmentSlotType.HEAD, player);
                    }
                    return true;
                }
            });
            y -= 18;
        }
        addSlot(new SlotItemHandler(playerInventory, 40, x, y + 18 * 5 + 4));
    }

    public PlayerInvContainer(@Nullable ContainerType<?> type, PlayerEntity player, PacketBuffer data, int id) {
        this(type, player, null, id, data);
    }

    private void layoutCurioSlots(PlayerEntity player, int x, int y, Collection<Integer> curioSlots, IItemHandler curiosInv) {
        if (player != null) {
            Map<String, CurioStackHandler> curios = CuriosAPI.getCuriosHandler(player).orElse(null).getCurioMap();
            if (curios == null) return;
            int temp = x;
            int g = 0;
            for (Map.Entry<String, CurioStackHandler> entry : curios.entrySet()) {
                if (g == 4) {
                    y -= 120;
                }
                if (g < 4) {
                    x = temp;
                } else {
                    x = -26 - 18 * entry.getValue().getSlots();
                }
                addSlotRange(entry.getValue(), 0, x, y, entry.getValue().getSlots(), 18);
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
    public boolean canInteractWith(PlayerEntity playerIn) {
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

    public static class ContainerProvider implements INamedContainerProvider {
        @Override
        public ITextComponent getDisplayName() {
            return new StringTextComponent("Player Inventory");
        }

        @Nullable
        @Override
        public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
            return new PlayerInvContainer(CURegistry.PLAYER_INV_CONTAINER.get(), player, WorldUtils.getRelatedContainer((ServerPlayerEntity) player), id, null);
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        if (!world.isRemote() && otherPlayer instanceof FakePlayer) {
            ((ServerWorld) world).getSaveHandler().writePlayerData(otherPlayer);
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
