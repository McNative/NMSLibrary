package org.mcnative.nms.library.impl;


import net.minecraft.server.v1_8_R3.*;

public class CustomTileEntityContainerAnvil implements ITileEntityContainer {
    private final World world;
    private final BlockPosition position;

    public CustomTileEntityContainerAnvil(World paramWorld, BlockPosition paramBlockPosition) {
        this.world = paramWorld;
        this.position = paramBlockPosition;
    }
    @Override
    public String getName() {
        return "anvil";
    }
    @Override
    public boolean hasCustomName() {
        return false;
    }
    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatMessage(Blocks.ANVIL.a() + ".name", new Object[0]);
    }

    @Override
    public Container createContainer(PlayerInventory paramPlayerInventory, EntityHuman paramEntityHuman) {
        return new CustomContainerAnvil(paramPlayerInventory, this.world, this.position, paramEntityHuman);
    }
    @Override
    public String getContainerName() {
        return "minecraft:anvil";
    }
}