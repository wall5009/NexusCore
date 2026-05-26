package com.rollylindenshnizzer.nexuscore.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiPredicate;

public class SimpleItemHandler {
    private final NonNullList<ItemStack> stacks;
    private BiPredicate<Integer, ItemStack> validator = (slot, stack) -> true;

    public SimpleItemHandler(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public int size() {
        return stacks.size();
    }

    public ItemStack get(int slot) {
        return stacks.get(slot);
    }

    public void set(int slot, ItemStack stack) {
        stacks.set(slot, stack);
    }

    public List<ItemStack> stacks() {
        return stacks;
    }

    public SimpleItemHandler validator(BiPredicate<Integer, ItemStack> validator) {
        this.validator = validator;
        return this;
    }

    public ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !validator.test(slot, stack)) {
            return stack;
        }
        ItemStack existing = stacks.get(slot);
        int limit = stack.getMaxStackSize();
        if (existing.isEmpty()) {
            int moved = Math.min(limit, stack.getCount());
            if (!simulate) {
                stacks.set(slot, stack.copyWithCount(moved));
            }
            return stack.copyWithCount(stack.getCount() - moved);
        }
        if (!ItemStack.isSameItemSameComponents(existing, stack)) {
            return stack;
        }
        int moved = Math.min(limit - existing.getCount(), stack.getCount());
        if (moved <= 0) {
            return stack;
        }
        if (!simulate) {
            existing.grow(moved);
        }
        return stack.copyWithCount(stack.getCount() - moved);
    }

    public ItemStack extract(int slot, int amount, boolean simulate) {
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = existing.copyWithCount(Math.min(amount, existing.getCount()));
        if (!simulate) {
            existing.shrink(extracted.getCount());
            if (existing.isEmpty()) {
                stacks.set(slot, ItemStack.EMPTY);
            }
        }
        return extracted;
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, stacks, provider);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider provider) {
        ContainerHelper.loadAllItems(tag, stacks, provider);
    }

    public ListTag saveList(HolderLookup.Provider provider) {
        return ContainerHelper.saveAllItems(new CompoundTag(), stacks, provider).getList("Items", 10);
    }
}
