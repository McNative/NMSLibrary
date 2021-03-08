package org.mcnative.nms.library.impl;

import net.minecraft.server.v1_8_R3.*;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.reflect.ReflectException;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mcnative.nms.library.api.InventoryVersionWrapper;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

public class InventoryVersionWrapper1_8_R3 implements InventoryVersionWrapper {

    @Override
    public int getNextContainerId(Player player, Object container) {
        return toNMS(player).nextContainerCounter();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, String guiTitle) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).activeContainer = (Container) container;
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {
        ((Container) container).windowId = containerId;
    }

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(Player player, String guiTitle) {
        EntityPlayer entityPlayer = toNMS(player);
        return new CustomContainerAnvil(entityPlayer.inventory, entityPlayer.world, new BlockPosition(0, 0, 0), entityPlayer);
    }

    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /*private static class AnvilContainer extends ContainerAnvil {

        private final Field gField = setAccessible(ReflectionUtil.getField(ContainerAnvil.class,  "g"));
        private final Field hField = setAccessible(ReflectionUtil.getField(ContainerAnvil.class,  "h"));
        private final Field mField = setAccessible(ReflectionUtil.getField(ContainerAnvil.class,  "m"));
        private final Field lField = setAccessible(ReflectionUtil.getField(ContainerAnvil.class,  "l"));
        private final Field kField = setAccessible(ReflectionUtil.getField(ContainerAnvil.class,  "k"));

        private final static BlockPosition blockPosition = new BlockPosition(0, 0, 0);

        public AnvilContainer(EntityHuman entityhuman) {
            super(entityhuman.inventory, entityhuman.world, blockPosition, entityhuman);
            System.out.println("create anvil container with level cost change slot change");

            Iterators.remove(this.c, slot -> slot.index == 2);

            IInventory g = getFieldValue(this,  gField, IInventory.class);

            this.a(new Slot(g, 2, 134, 47) {
                public boolean isAllowed(ItemStack itemstack) {
                    return false;
                }

                public boolean isAllowed(EntityHuman entityhuman) {
                    return true;//(entityhuman.abilities.canInstantlyBuild || entityhuman.expLevel >= a) && a > 0 && this.hasItem();
                }

                public void a(EntityHuman entityhuman, ItemStack itemstack) {
                    if (!entityhuman.abilities.canInstantlyBuild) {
                        //entityhuman.levelDown(-a);
                    }

                    IInventory h = getFieldValue(this, hField, IInventory.class);

                    World world = entityhuman.world;
                    h.setItem(0, (ItemStack)null);

                    int k = getFieldValue(this, kField, int.class);
                    if (k > 0) {
                        ItemStack itemstack1 = h.getItem(1);
                        if (itemstack1 != null && itemstack1.count > k) {
                            itemstack1.count -= k;
                            h.setItem(1, itemstack1);
                        } else {
                            h.setItem(1, (ItemStack)null);
                        }
                    } else {
                        h.setItem(1, (ItemStack)null);
                    }

                    a = 0;
                    /*IBlockData iblockdata = world.getType(blockPosition);
                    if (!entityhuman.abilities.canInstantlyBuild && !world.isClientSide && iblockdata.getBlock() == Blocks.ANVIL && entityhuman.bc().nextFloat() < 0.12F) {
                        int i = (Integer)iblockdata.get(BlockAnvil.DAMAGE);
                        ++i;
                        if (i > 2) {
                            world.setAir(blockPosition);
                            world.triggerEffect(1020, blockPosition, 0);
                        } else {
                            world.setTypeAndData(blockPosition, iblockdata.set(BlockAnvil.DAMAGE, i), 2);
                            world.triggerEffect(1021, blockPosition, 0);
                        }
                    } else if (!world.isClientSide) {
                        world.triggerEffect(1021, blockPosition, 0);
                    }*

                }
            });
        }

        private static Field setAccessible(Field field) {
            field.setAccessible(true);
            return field;
        }

        private static <R> R getFieldValue(Object object, Field field, Class<R> value){
            try {
                return value.cast(field.get(object));
            } catch (Exception exception) {throw new ReflectException(exception);}
        }


        public static void changeFieldValue(Object object,Field field,Object value){
            try {
                if(field != null){
                    field.set(object,value);
                }
            } catch (Exception exception) {throw new ReflectException("Could not change file "+field.getName()+" from class "+field.getType(),exception);}
        }

        @Override
        public boolean a(EntityHuman human) {
            return true;
        }

        @Override
        public void b(EntityHuman entityhuman) {
        }

        /*public void hgf() {
            ItemStack itemstack = this.repairInventory.getItem(0);

            this.levelCost = 1;
            int i = 0;
            byte b0 = 0;
            byte b1 = 0;

            if (itemstack.isEmpty()) {
                CraftEventFactory.callPrepareAnvilEvent((InventoryView)getBukkitView(), ItemStack.a);
                this.levelCost = 0;
            } else {
                ItemStack itemstack1 = itemstack.cloneItemStack();
                ItemStack itemstack2 = this.repairInventory.getItem(1);
                Map<Enchantment, Integer> map = EnchantmentManager.a(itemstack1);
                int j = b0 + itemstack.getRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getRepairCost());

                this.k = 0;
                if (!itemstack2.isEmpty()) {
                    boolean flag = (itemstack2.getItem() == Items.ENCHANTED_BOOK && !ItemEnchantedBook.e(itemstack2).isEmpty());




                    if (itemstack1.e() && itemstack1.getItem().a(itemstack, itemstack2)) {
                        int k = Math.min(itemstack1.getDamage(), itemstack1.h() / 4);
                        if (k <= 0) {
                            CraftEventFactory.callPrepareAnvilEvent((InventoryView)getBukkitView(), ItemStack.a);
                            this.levelCost = 0;
                            return;
                        }
                        int i1;
                        for (i1 = 0; k > 0 && i1 < itemstack2.getCount(); i1++) {
                            int l = itemstack1.getDamage() - k;
                            itemstack1.setDamage(l);
                            i++;
                            k = Math.min(itemstack1.getDamage(), itemstack1.h() / 4);
                        }

                        this.k = i1;
                    } else {
                        if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.e())) {
                            CraftEventFactory.callPrepareAnvilEvent((InventoryView)getBukkitView(), ItemStack.a);
                            this.levelCost = 0;

                            return;
                        }
                        if (itemstack1.e() && !flag) {
                            int k = itemstack.h() - itemstack.getDamage();
                            int i1 = itemstack2.h() - itemstack2.getDamage();
                            int l = i1 + itemstack1.h() * 12 / 100;
                            int j1 = k + l;
                            int k1 = itemstack1.h() - j1;

                            if (k1 < 0) {
                                k1 = 0;
                            }

                            if (k1 < itemstack1.getDamage()) {
                                itemstack1.setDamage(k1);
                                i += 2;
                            }
                        }

                        Map<Enchantment, Integer> map1 = EnchantmentManager.a(itemstack2);
                        boolean flag1 = false;
                        boolean flag2 = false;
                        Iterator<Enchantment> iterator = map1.keySet().iterator();

                        while (iterator.hasNext()) {
                            Enchantment enchantment = iterator.next();

                            if (enchantment != null) {
                                int l1 = map.containsKey(enchantment) ? ((Integer)map.get(enchantment)).intValue() : 0;
                                int i2 = ((Integer)map1.get(enchantment)).intValue();

                                i2 = (l1 == i2) ? (i2 + 1) : Math.max(i2, l1);
                                boolean flag3 = enchantment.canEnchant(itemstack);

                                if (this.player.abilities.canInstantlyBuild || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                                    flag3 = true;
                                }

                                Iterator<Enchantment> iterator1 = map.keySet().iterator();

                                while (iterator1.hasNext()) {
                                    Enchantment enchantment1 = iterator1.next();

                                    if (enchantment1 != enchantment && !enchantment.b(enchantment1)) {
                                        flag3 = false;
                                        i++;
                                    }
                                }

                                if (!flag3) {
                                    flag2 = true; continue;
                                }
                                flag1 = true;
                                if (i2 > enchantment.getMaxLevel()) {
                                    i2 = enchantment.getMaxLevel();
                                }

                                map.put(enchantment, Integer.valueOf(i2));
                                int j2 = 0;

                                switch (enchantment.d()) {
                                    case null:
                                        j2 = 1;
                                        break;
                                    case UNCOMMON:
                                        j2 = 2;
                                        break;
                                    case RARE:
                                        j2 = 4;
                                        break;
                                    case VERY_RARE:
                                        j2 = 8;
                                        break;
                                }
                                if (flag) {
                                    j2 = Math.max(1, j2 / 2);
                                }

                                i += j2 * i2;
                                if (itemstack.getCount() > 1) {
                                    i = 40;
                                }
                            }
                        }


                        if (flag2 && !flag1) {
                            CraftEventFactory.callPrepareAnvilEvent((InventoryView)getBukkitView(), ItemStack.a);
                            this.levelCost = 0;

                            return;
                        }
                    }
                }
                if (StringUtils.isBlank(this.renameText)) {
                    if (itemstack.hasName()) {
                        b1 = 1;
                        i += b1;
                        itemstack1.r();
                    }
                } else if (!this.renameText.equals(itemstack.getName().getString())) {
                    b1 = 1;
                    i += b1;
                    itemstack1.a(new ChatComponentText(this.renameText));
                }

                this.levelCost = j + i;
                if (i <= 0) {
                    itemstack1 = ItemStack.a;
                }

                if (b1 == i && b1 > 0 && this.levelCost >= this.maximumRepairCost) {
                    this.levelCost = this.maximumRepairCost - 1;
                }

                if (this.levelCost >= this.maximumRepairCost && !this.player.abilities.canInstantlyBuild) {
                    itemstack1 = ItemStack.a;
                }

                if (!itemstack1.isEmpty()) {
                    int k2 = itemstack1.getRepairCost();

                    if (!itemstack2.isEmpty() && k2 < itemstack2.getRepairCost()) {
                        k2 = itemstack2.getRepairCost();
                    }

                    if (b1 != i || b1 == 0) {
                        k2 = k2 * 2 + 1;
                    }

                    itemstack1.setRepairCost(k2);
                    EnchantmentManager.a(map, itemstack1);
                }

                CraftEventFactory.callPrepareAnvilEvent((InventoryView)getBukkitView(), itemstack1);
                b();
            }
        }*

        @Override
        public void addSlotListener(ICrafting icrafting) {
            if (this.listeners.contains(icrafting)) {
                throw new IllegalArgumentException("Listener already listening");
            } else {
                this.listeners.add(icrafting);
                icrafting.a(this, this.a());
                this.b();
            }
            icrafting.setContainerData(this, 0, 35);
        }

        @Override
        public void e() {
            System.out.println("e() void");
            IInventory g = getFieldValue(this,  gField, IInventory.class);
            IInventory h = getFieldValue(this, hField, IInventory.class);
            EntityHuman m = getFieldValue(this, mField, EntityHuman.class);
            String l2 = getFieldValue(this, lField, String.class);


            ItemStack itemstack = h.getItem(0);
            this.a = 35;//1
            int i = 0;
            byte b0 = 0;
            byte b1 = 0;
            if (itemstack == null) {
                g.setItem(0, (ItemStack)null);
                this.a = 0;
            } else {
                ItemStack itemstack1 = itemstack.cloneItemStack();
                ItemStack itemstack2 = h.getItem(1);
                Map map = EnchantmentManager.a(itemstack1);
                boolean flag7 = false;
                int j = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());

                ///this.k = 0;
                changeFieldValue(this, kField, 35);

                int k;
                if (itemstack2 != null) {
                    flag7 = itemstack2.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.h(itemstack2).size() > 0;
                    int l;
                    int i1;
                    if (itemstack1.e() && itemstack1.getItem().a(itemstack, itemstack2)) {
                        k = Math.min(itemstack1.h(), itemstack1.j() / 4);
                        if (k <= 0) {
                            g.setItem(0, (ItemStack)null);
                            this.a = 0;
                            return;
                        }

                        for(l = 0; k > 0 && l < itemstack2.count; ++l) {
                            i1 = itemstack1.h() - k;
                            itemstack1.setData(35);//i1
                            ++i;
                            k = Math.min(itemstack1.h(), itemstack1.j() / 4);
                        }

                        k = l;
                    } else {
                        if (!flag7 && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.e())) {
                            g.setItem(0, (ItemStack)null);
                            this.a = 0;
                            return;
                        }

                        int j1;
                        if (itemstack1.e() && !flag7) {
                            k = itemstack.j() - itemstack.h();
                            l = itemstack2.j() - itemstack2.h();
                            i1 = l + itemstack1.j() * 12 / 100;
                            int k1 = k + i1;
                            j1 = itemstack1.j() - k1;
                            if (j1 < 0) {
                                j1 = 0;
                            }

                            if (j1 < itemstack1.getData()) {
                                itemstack1.setData(35);//j1
                                i += 2;
                            }
                        }

                        Map map1 = EnchantmentManager.a(itemstack2);
                        Iterator iterator = map1.keySet().iterator();

                        label149:
                        while(true) {
                            Enchantment enchantment;
                            do {
                                if (!iterator.hasNext()) {
                                    break label149;
                                }

                                i1 = (Integer)iterator.next();
                                enchantment = Enchantment.getById(i1);
                            } while(enchantment == null);

                            j1 = map.containsKey(i1) ? (Integer)map.get(i1) : 0;
                            int l1 = (Integer)map1.get(i1);
                            int i2;
                            if (j1 == l1) {
                                ++l1;
                                i2 = l1;
                            } else {
                                i2 = Math.max(l1, j1);
                            }

                            l1 = i2;
                            boolean flag8 = enchantment.canEnchant(itemstack);
                            if (m.abilities.canInstantlyBuild || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                                flag8 = true;
                            }

                            Iterator iterator1 = map.keySet().iterator();

                            int k2;
                            while(iterator1.hasNext()) {
                                k2 = (Integer)iterator1.next();
                                if (k2 != i1 && !enchantment.a(Enchantment.getById(k2))) {
                                    flag8 = false;
                                    ++i;
                                }
                            }

                            if (flag8) {
                                if (i2 > enchantment.getMaxLevel()) {
                                    l1 = enchantment.getMaxLevel();
                                }

                                map.put(i1, l1);
                                k2 = 0;
                                switch(enchantment.getRandomWeight()) {
                                    case 1:
                                        k2 = 8;
                                        break;
                                    case 2:
                                        k2 = 4;
                                    case 3:
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    default:
                                        break;
                                    case 5:
                                        k2 = 2;
                                        break;
                                    case 10:
                                        k2 = 1;
                                }

                                if (flag7) {
                                    k2 = Math.max(1, k2 / 2);
                                }

                                i += k2 * l1;
                            }
                        }
                    }
                }

                if (StringUtils.isBlank(l2)) {
                    if (itemstack.hasName()) {
                        b1 = 1;
                        i += b1;
                        itemstack1.r();
                    }
                } else if (!l2.equals(itemstack.getName())) {
                    b1 = 1;
                    i += b1;
                    itemstack1.c(l2);
                }

                this.a = 35;//j + i
                if (i <= 0) {
                    itemstack1 = null;
                }

                if (b1 == i && b1 > 0 && this.a >= 40) {
                    this.a = 35;//39
                }

                if (this.a >= 40 && !m.abilities.canInstantlyBuild) {
                    itemstack1 = null;
                }

                if (itemstack1 != null) {
                    k = itemstack1.getRepairCost();
                    if (itemstack2 != null && k < itemstack2.getRepairCost()) {
                        k = itemstack2.getRepairCost();
                    }

                    k = k * 2 + 1;
                    itemstack1.setRepairCost(35);//k
                    EnchantmentManager.a(map, itemstack1);
                }

                g.setItem(0, itemstack1);
                this.b();
            }
        }
    }*/
}
