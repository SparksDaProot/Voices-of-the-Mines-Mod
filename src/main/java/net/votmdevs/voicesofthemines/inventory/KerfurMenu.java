package net.votmdevs.voicesofthemines.inventory;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class KerfurMenu extends AbstractContainerMenu {
    private final Container container;
    public final String kerfurColor;
    public final boolean isOmega; // ДОБАВЛЕНО: Флаг для Омеги
    private final int containerRows;

    public KerfurMenu(int containerId, Inventory playerInventory, Container container, String color, boolean isOmega) {
        super(KerfurMod.KERFUR_MENU.get(), containerId);
        this.container = container;
        this.kerfurColor = color;
        this.isOmega = isOmega;
        this.containerRows = isOmega ? 6 : 3; // 6 рядов для Омеги, 3 для обычного

        checkContainerSize(container, containerRows * 9);
        container.startOpen(playerInventory.player);

        // slots
        for (int row = 0; row < containerRows; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Kerfur-O
        int playerInvY = isOmega ? 140 : 84;
        int hotbarY = isOmega ? 198 : 142;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        int containerSize = isOmega ? 54 : 27; // Учитываем новый размер

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < containerSize) {
                if (!this.moveItemStackTo(itemstack1, containerSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, containerSize, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}