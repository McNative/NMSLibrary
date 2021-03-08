package org.mcnative.nms.library.impl;


import net.minecraft.server.v1_8_R3.*;

public class CustomTileEntityContainerAnvil implements ITileEntityContainer {
    private final World world;
    private final BlockPosition position;

    public CustomTileEntityContainerAnvil(World paramWorld, BlockPosition paramBlockPosition) {
        this.world = paramWorld;
        this.position = paramBlockPosition;
    }

    public String getName() {
        return "anvil";
    }

    public boolean hasCustomName() {
        return false;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatMessage(Blocks.ANVIL.a() + ".name", new Object[0]);
    }

    public Container createContainer(PlayerInventory paramPlayerInventory, EntityHuman paramEntityHuman) {
        return new CustomContainerAnvil(paramPlayerInventory, this.world, this.position, paramEntityHuman);
    }

    public String getContainerName() {
        return "minecraft:anvil";
    }
}