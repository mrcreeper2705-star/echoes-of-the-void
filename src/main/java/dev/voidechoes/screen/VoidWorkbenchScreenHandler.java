package dev.voidechoes.screen;

import dev.voidechoes.registry.ModBlocks;
import dev.voidechoes.registry.ModRecipes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/** 5x5 crafting grid. Slot 0 = result, 1-25 = grid, 26-52 = inventory, 53-61 = hotbar. */
public class VoidWorkbenchScreenHandler extends ScreenHandler {
    public static final int SIZE = 5;
    private static final int INV_START = 26, HOTBAR_START = 53, END = 62;

    private final RecipeInputInventory input = new CraftingInventory(this, SIZE, SIZE);
    private final CraftingResultInventory result = new CraftingResultInventory();
    private final ScreenHandlerContext context;
    private final PlayerEntity player;

    public VoidWorkbenchScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, ScreenHandlerContext.EMPTY);
    }

    public VoidWorkbenchScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext context) {
        super(ModScreens.VOID_WORKBENCH, syncId);
        this.context = context;
        this.player = inv.player;
        this.addSlot(new ResultSlot(result, 0, 138, 53));
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                this.addSlot(new Slot(input, c + r * SIZE, 17 + c * 18, 17 + r * 18));
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 122 + r * 18));
        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(inv, c, 8 + c * 18, 180));
    }

    private void updateResult(World world) {
        if (world.isClient) return;
        CraftingRecipeInput in = input.createRecipeInput();
        ItemStack out = world.getRecipeManager().getFirstMatch(ModRecipes.VOID_CRAFTING, in, world)
                .map(e -> e.value().craft(in, world.getRegistryManager()))
                .orElse(ItemStack.EMPTY);
        result.setStack(0, out);
        this.setPreviousTrackedSlot(0, out);
        if (player instanceof ServerPlayerEntity sp) {
            sp.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 0, out));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        if (inventory == input) context.run((world, pos) -> updateResult(world));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();
            if (index == 0) {
                if (!this.insertItem(stack, INV_START, END, true)) return ItemStack.EMPTY;
                slot.onQuickTransfer(stack, copy);
            } else if (index >= INV_START) {
                if (!this.insertItem(stack, 1, INV_START, false)) {
                    if (index < HOTBAR_START) {
                        if (!this.insertItem(stack, HOTBAR_START, END, false)) return ItemStack.EMPTY;
                    } else if (!this.insertItem(stack, INV_START, HOTBAR_START, false)) return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(stack, INV_START, END, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTakeItem(player, stack);
            if (index == 0) player.dropItem(stack, false);
        }
        return copy;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != result && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        context.run((world, pos) -> this.dropInventory(player, input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, ModBlocks.VOID_WORKBENCH);
    }

    private class ResultSlot extends Slot {
        ResultSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            stack.onCraftByPlayer(player.getWorld(), player, stack.getCount());
            for (int i = 0; i < input.size(); i++) {
                ItemStack in = input.getStack(i);
                if (in.isEmpty()) continue;
                ItemStack remainder = in.getItem().hasRecipeRemainder()
                        ? new ItemStack(in.getItem().getRecipeRemainder()) : ItemStack.EMPTY;
                input.removeStack(i, 1);
                if (remainder.isEmpty()) continue;
                if (input.getStack(i).isEmpty()) input.setStack(i, remainder);
                else if (!player.getInventory().insertStack(remainder)) player.dropItem(remainder, false);
            }
            super.onTakeItem(player, stack);
            context.run((world, pos) -> updateResult(world));
        }
    }
}
