package org.mcnative.nms.library.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_8_R3.BlockAnvil;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.Enchantment;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.ICrafting;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.InventoryCraftResult;
import net.minecraft.server.v1_8_R3.InventorySubcontainer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import net.minecraft.server.v1_8_R3.Slot;
import net.minecraft.server.v1_8_R3.World;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.mcnative.nms.library.api.NMSHelper;

public class CustomContainerAnvil extends Container {

    private final IInventory resultSlot = new InventoryCraftResult();
    private final IInventory processSlots = new CustomContainerAnvilInventory(this, "Repair", true, 2);
    private final World inWorld;
    private final BlockPosition position;
    public int expCost;
    private int iDontKnow;
    private String textbox;
    private final EntityHuman human;
    private CraftInventoryView bukkitEntity = null;
    private final PlayerInventory playerInventory;

    public CustomContainerAnvil(PlayerInventory playerinventory, World world, BlockPosition blockposition, EntityHuman entityhuman) {
        this.playerInventory = playerinventory;
        this.position = blockposition;
        this.inWorld = world;
        this.human = entityhuman;
        setSlot(new Slot(processSlots, 0, 27, 47));
        setSlot(new Slot(processSlots, 1, 76, 47));
        setSlot(new CustomSlotAnvilResult(this, resultSlot, 2, 134, 47, world, blockposition));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                setSlot(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            setSlot(new Slot(playerinventory, i, 8 + i * 18, 142));
        }
    }

    @NMSHelper
    public Slot setSlot(Slot slot) {
        return a(slot);
    }

    @NMSHelper
    public void onAnvilInventoryClick(IInventory inventory) {
        super.a(inventory);
        if (inventory == processSlots) {
            //updateAnvilDisplay();
            this.expCost = 27;

            org.bukkit.inventory.ItemStack bukkitItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND);
            ItemStack resultItem = CraftItemStack.asNMSCopy(bukkitItem);
            resultItem.setRepairCost(expCost);

            resultSlot.setItem(0, resultItem);

            for (ICrafting listener : this.listeners) {
                listener.setContainerData(this, 0, expCost);
            }
        }
    }


    @NMSHelper
    public void onAnvilClose(EntityHuman entityhuman) {
/*
        super.b(entityhuman);
        if (!inWorld.isClientSide) {
            for (int i = 0; i < processSlots.getSize(); i++) {
                ItemStack itemstack = processSlots.splitWithoutUpdate(i);
                if (itemstack != null) {
                    entityhuman.drop(itemstack, false);
                }
            }
        }
         */
    }

    /**
     * Called when the player opens this inventory.
     */
    @Override
    public void addSlotListener(ICrafting icrafting) {
        super.addSlotListener(icrafting);
        System.out.println(expCost);
        icrafting.setContainerData(this, 0, expCost);//expCost
    }

    @NMSHelper
    public boolean canOpenAnvilInventory(EntityHuman entityHuman) {
        return true;
    }

    @NMSHelper
    public ItemStack onShiftClick(EntityHuman entityhuman, int index) {
        ItemStack itemResult = null;
        Slot slot = c.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack inSlot = slot.getItem();

            itemResult = inSlot.cloneItemStack();
            if (index == 2) {
                if (!a(inSlot, 3, 39, true)) { return null; }
                slot.a(inSlot, itemResult);
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 39 && !a(inSlot, 0, 2, false)) { return null; }
            } else if (!a(inSlot, 3, 39, false)) { return null; }
            if (inSlot.count == 0) {
                slot.set(null);
            } else {
                slot.f();
            }
            if (inSlot.count == itemResult.count) { return null; }
            slot.a(entityhuman, inSlot);
        }
        return itemResult;
    }

    public void updateAnvilDisplay() {
        System.out.println("updateAnvilDisplay");
        ItemStack leftSlot = processSlots.getItem(0);

        //if(leftSlot != null) leftSlot.setRepairCost(35);//

        expCost = 1;//1
        int reRepairCostAddition = 0;
        byte costOffsetModifier = 0;

        // If the item in the left-most slot is null...
        if (leftSlot == null) {
            // Make sure we don't have a result item.
            resultSlot.setItem(0, (ItemStack) null);
            expCost = 0;//0
        } else {
            ItemStack resultItem = leftSlot.cloneItemStack();
            ItemStack rightSlot = processSlots.getItem(1);
            Map<Integer, Integer> leftEnchantments = EnchantmentManager.a(resultItem);
            boolean usingEnchantedBook;
            int existingReRepairCost = leftSlot.getRepairCost() + (rightSlot == null ? 0 : rightSlot.getRepairCost());

            iDontKnow = 0;
            // If we have an item in the right-most slot...
            if (rightSlot != null) {
                //rightSlot.setRepairCost(35);//
                usingEnchantedBook = rightSlot.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.h(rightSlot).size() > 0;
                if (resultItem.e() && resultItem.getItem().a(leftSlot, rightSlot)) {
                    int k = Math.min(resultItem.h(), resultItem.j() / 4);
                    if (k <= 0) {
                        resultSlot.setItem(0, (ItemStack) null);
                        expCost = 0;//0
                        return;
                    }
                    int someVariable;
                    for (someVariable = 0; k > 0 && someVariable < rightSlot.count; someVariable++) {
                        int new_durability = resultItem.h() - k;
                        resultItem.setData(new_durability);
                        reRepairCostAddition++;
                        k = Math.min(resultItem.h(), resultItem.j() / 4);
                    }
                    iDontKnow = someVariable;
                } else {
                    // If we're not apply an enchantment and we're not repairing
                    // an item...
                    if (!usingEnchantedBook && (resultItem.getItem() != rightSlot.getItem() || !resultItem.e())) {
                        // Make sure we don't have a result item.
                        resultSlot.setItem(0, (ItemStack) null);
                        expCost = 0;//0
                        return;
                    }

                    // If we're not using an enchanted book (therefore, we must
                    // be repairing at this point)...
                    if (resultItem.e() && !usingEnchantedBook) {
                        // Compute the new durability. Max durability-damage
                        int leftDurability = leftSlot.j() - leftSlot.h();
                        int rightDurability = rightSlot.j() - rightSlot.h();
                        int i1 = rightDurability + resultItem.j() * 12 / 100;
                        int k1 = leftDurability + i1;

                        int j1 = resultItem.j() - k1;
                        if (j1 < 0) {
                            j1 = 0;
                        }
                        if (j1 < resultItem.getData()) {
                            resultItem.setData(j1);
                            reRepairCostAddition += 2;
                        }
                    }

                    Map<Integer, Integer> rightEnchantments = EnchantmentManager.a(rightSlot);
                    for (int enchantmentID : rightEnchantments.keySet()) {
                        Enchantment enchantment = Enchantment.getById(enchantmentID);
                        if(enchantment != null) {
                            // Compute a new enchantment level.
                            int leftLevel = leftEnchantments.getOrDefault(
                                    enchantmentID, 0);
                            int rightLevel = rightEnchantments.get(enchantmentID);
                            int newLevel;
                            if(leftLevel == rightLevel) {
                                rightLevel++;
                                newLevel = rightLevel;
                            } else {
                                newLevel = Math.max(rightLevel, leftLevel);
                            }
                            rightLevel = newLevel;
                            boolean enchantable = enchantment.canEnchant(leftSlot);
                            if(human.abilities.canInstantlyBuild || leftSlot.getItem() == Items.ENCHANTED_BOOK) {
                                enchantable = true;
                            }
                            for (Integer enchantID : leftEnchantments.keySet()) {
                                if(enchantID != enchantmentID && !enchantment.a(Enchantment.getById(enchantID))) {
                                    enchantable = false;
                                    reRepairCostAddition++;
                                }
                            }
                            if(enchantable) {
                                // Make sure we don't apply a level too high.
                                if(rightLevel > enchantment.getMaxLevel()) {
                                    rightLevel = enchantment.getMaxLevel();
                                }
                                leftEnchantments.put(Integer.valueOf(enchantmentID), Integer.valueOf(rightLevel));
                                int randomCostModifier = 0;
                                switch (enchantment.getRandomWeight()) {
                                    case 1:
                                        randomCostModifier = 8;
                                        break;
                                    case 2:
                                        randomCostModifier = 4;
                                    case 3:
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    default:
                                        break;
                                    case 5:
                                        randomCostModifier = 2;
                                        break;
                                    case 10:
                                        randomCostModifier = 1;
                                }
                                if(usingEnchantedBook) {
                                    randomCostModifier = Math.max(1, randomCostModifier / 2);
                                }
                                reRepairCostAddition += randomCostModifier * rightLevel;
                            }
                        }
                    }
                }
            }

            // If the textbox is blank...
            if (StringUtils.isBlank(textbox)) {
                // Remove the name on our item.
                if (leftSlot.hasName()) {
                    costOffsetModifier = 1;
                    reRepairCostAddition += costOffsetModifier;
                    resultItem.r();//Remove the name of item
                }
            } else if (!textbox.equals(leftSlot.getName())) {
                // Name the item.
                costOffsetModifier = 1;
                reRepairCostAddition += costOffsetModifier;
                resultItem.c(textbox);//Set name to item
                this.textbox = leftSlot.getName();
            }

            // Apply the costs for re-repairing the items.
            expCost = existingReRepairCost + reRepairCostAddition;;//
            if (reRepairCostAddition <= 0) {
                resultItem = null;
            }
            //Max cost check
            if (costOffsetModifier == reRepairCostAddition && costOffsetModifier > 0 && expCost >= 40) {
                expCost = 39;//39
            }

            // Max out at exp-cost 40 repairs.
            if (expCost >= 40 && !human.abilities.canInstantlyBuild) {
                resultItem = null;
            }

            // Apply everything to our result item.
            if (resultItem != null) {
                int repairCost = resultItem.getRepairCost();
                if (rightSlot != null && repairCost < rightSlot.getRepairCost()) {
                    repairCost = rightSlot.getRepairCost();
                }
                repairCost = repairCost * 2 + 1;
                resultItem.setRepairCost(repairCost);//repairCost
                EnchantmentManager.a(leftEnchantments, resultItem);//Apply enchantments to resultItem
                System.out.println("repairCost:"+resultItem.getRepairCost());
            } else System.out.println("not result");

            resultSlot.setItem(0, resultItem);
            b();
            System.out.println("Update " + this.listeners.size() + ":" + expCost + ":" + iDontKnow);
            /*for (ICrafting listener : this.listeners) {
                listener.setContainerData(this, 0, 35);
            }*/
        }
    }

    /**
     * This is called when a player places an item in the anvil inventory.
     */
    @Override
    public void a(IInventory iinventory) {
        onAnvilInventoryClick(iinventory);
    }

    /**
     * Called when the player closes this inventory.
     */
    @Override
    public void b(EntityHuman entityhuman) {
        this.onAnvilClose(entityhuman);
    }

    /**
     * Called while this is open.
     *
     * @return TRUE if the player can view this inventory, FALSE if the player
     *  must close the inventory.
     */
    @Override
    public boolean a(EntityHuman entityhuman) {
        /*
        if (!checkReachable) { return true; }
        return inWorld.getType(position).getBlock() == Blocks.ANVIL;
         */
        return canOpenAnvilInventory(entityhuman);
    }

    /**
     * Called when shift-clicking an item into this inventory.
     */
    @Override
    public ItemStack b(EntityHuman entityhuman, int index) {
        return onShiftClick(entityhuman, index);
    }

    @NMSHelper
    static IInventory getProcessSlots(CustomContainerAnvil containerAnvil) {
        return containerAnvil.processSlots;
    }

    static IInventory a(CustomContainerAnvil containerAnvil) {
        return getProcessSlots(containerAnvil);
    }

    static int b(CustomContainerAnvil containerAnvil) {
        return containerAnvil.iDontKnow;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) { return bukkitEntity; }
        CraftInventory inventory = new CraftInventoryAnvil(processSlots, resultSlot);
        bukkitEntity = new CraftInventoryView(playerInventory.player.getBukkitEntity(), inventory, this);
        return bukkitEntity;
    }

    private static class CustomContainerAnvilInventory extends InventorySubcontainer {

        final CustomContainerAnvil anvil;
        public List<HumanEntity> transaction = new ArrayList<>();
        public Player player;
        private int maxStack = 64;

        @Override
        public ItemStack[] getContents() {
            return items;
        }

        @Override
        public void onOpen(CraftHumanEntity who) {
            transaction.add(who);
        }

        @Override
        public void onClose(CraftHumanEntity who) {
            transaction.remove(who);
        }

        @Override
        public List<HumanEntity> getViewers() {
            return transaction;
        }

        @Override
        public InventoryHolder getOwner() {
            return player;
        }

        @Override
        public void setMaxStackSize(int size) {
            maxStack = size;
        }

        CustomContainerAnvilInventory(CustomContainerAnvil containerAnvil, String title, boolean customName, int size) {
            super(title, customName, size);
            anvil = containerAnvil;
        }

        @Override
        public void update() {
            super.update();
            anvil.onAnvilInventoryClick(this);
        }

        @Override
        public int getMaxStackSize() {
            return maxStack;
        }
    }

    private static class CustomSlotAnvilResult extends Slot {

        private final CustomContainerAnvil anvil;
        private final World world;
        private final BlockPosition position;

        CustomSlotAnvilResult(CustomContainerAnvil paramContainerAnvil, IInventory paramIInventory, int paramInt1, int paramInt2, int paramInt3,
                              World paramWorld, BlockPosition paramBlockPosition) {
            super(paramIInventory, paramInt1, paramInt2, paramInt3);

            anvil = paramContainerAnvil;
            world = paramWorld;
            position = paramBlockPosition;
        }

        /**
         * Whether or not an ItemStack is allowed to be placed in this slot.
         */
        @Override
        public boolean isAllowed(ItemStack paramItemStack) {
            return false;
        }

        /**
         * Whether or not a human is allowed to click in this slot.
         */
        @Override
        public boolean isAllowed(EntityHuman paramEntityHuman) {
            return (paramEntityHuman.abilities.canInstantlyBuild || paramEntityHuman.expLevel >= anvil.expCost) && anvil.expCost > 0 && hasItem();
        }

        @NMSHelper
        public void onResultTake(EntityHuman entityHuman, ItemStack itemStack) {
            if (!entityHuman.abilities.canInstantlyBuild) {
                entityHuman.levelDown(-anvil.expCost);
            }
            // Take the item.
            CustomContainerAnvil.a(anvil).setItem(0, null);
            if (CustomContainerAnvil.b(anvil) > 0) {
                ItemStack resultObject = CustomContainerAnvil.a(anvil).getItem(1);
                if (resultObject != null && resultObject.count > CustomContainerAnvil.b(anvil)) {
                    resultObject.count -= CustomContainerAnvil.b(anvil);
                    CustomContainerAnvil.a(anvil).setItem(1, resultObject);
                } else {
                    CustomContainerAnvil.a(anvil).setItem(1, null);
                }
            } else {
                CustomContainerAnvil.a(anvil).setItem(1, null);
            }
            anvil.expCost = 0;

            damageAnvil(entityHuman);
        }

        public void damageAnvil(EntityHuman entityHuman) {
            IBlockData block = world.getType(position);
            if (!entityHuman.abilities.canInstantlyBuild && !world.isClientSide
                    && block != null && block.getBlock() == Blocks.ANVIL
                    && block.getBlock() != null && entityHuman.bc().nextFloat() < 0.12F) {

                int damage = block.get(BlockAnvil.DAMAGE);
                damage++;
                if (damage > 2) {
                    world.setAir(position);
                    world.triggerEffect(1020, position, 0);
                } else {
                    world.setTypeAndData(position, block.set(BlockAnvil.DAMAGE, damage), 2);
                    world.triggerEffect(1021, position, 0);
                }
            } else if (!world.isClientSide) {
                world.triggerEffect(1021, position, 0);
            }
        }

        /**
         * Called when the player takes the result item from the anvil.
         */
        @Override
        public void a(EntityHuman entityHuman, ItemStack itemStack) {
            onResultTake(entityHuman, itemStack);
        }
    }
}