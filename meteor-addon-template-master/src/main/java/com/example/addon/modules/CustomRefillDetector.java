package com.example.addon.modules;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomRefillDetector extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.getRenderGroup();

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
            .name("radius")
            .description("Scan radius around player.")
            .defaultValue(15)
            .min(5)
            .sliderMax(30)
            .build()
    );

    private final Setting<Integer> minPillarHeight = sgGeneral.add(new IntSetting.Builder()
            .name("min-pillar-height")
            .description("Minimum height of suspicious pillar to alert.")
            .defaultValue(3)
            .min(2)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> notifyChat = sgGeneral.add(new BoolSetting.Builder()
            .name("notify-chat")
            .description("Send alert to chat when base pillar is found.")
            .defaultValue(true)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .defaultValue(new SettingColor(255, 0, 0, 75))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .defaultValue(new SettingColor(255, 0, 0, 255))
            .build()
    );

    private final List<Block> targetBlocks = Arrays.asList(
            Blocks.GRAVEL,
            Blocks.STONE,
            Blocks.ANDESITE,
            Blocks.GRANITE,
            Blocks.DIORITE
    );

    private final List<BlockPos> detectedPillars = new ArrayList<>();
    private int timer = 0;

    public CustomRefillDetector(Category category) {
        super(category, "custom-refill-detector", "Detects suspicious refill pillars to locate hidden bases.");
    }

    @Override
    public void onActivate() {
        detectedPillars.clear();
        timer = 0;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (MeteorClient.mc.player == null || MeteorClient.mc.world == null) return;

        timer++;
        if (timer >= 20) {
            timer = 0;
            scanForBasePillars();
        }

        for (BlockPos pos : detectedPillars) {
            event.renderer.box(pos, sideColor.get(), lineColor.get(), ShapeMode.Both, 0);
        }
    }

    private void scanForBasePillars() {
        detectedPillars.clear();
        if (MeteorClient.mc.player == null) return;

        BlockPos playerPos = MeteorClient.mc.player.getBlockPos();
        int r = radius.get();

        List<BlockPos> susBlocks = new ArrayList<>();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (isSusBlock(pos)) {
                        susBlocks.add(pos);
                    }
                }
            }
        }

        for (BlockPos pos : susBlocks) {
            if (!susBlocks.contains(pos.down())) {
                int height = 0;
                BlockPos currentPos = pos;

                while (susBlocks.contains(currentPos)) {
                    height++;
                    currentPos = currentPos.up();
                }

                if (height >= minPillarHeight.get()) {
                    for (int i = 0; i < height; i++) {
                        detectedPillars.add(pos.up(i));
                    }

                    if (notifyChat.get()) {
                        ChatUtils.info("Detected suspicious pillar (Base) height " + height + " at: X=" 
                                + pos.getX() + " Y=" + pos.getY() + " Z=" + pos.getZ());
                    }
                }
            }
        }
    }

    private boolean isSusBlock(BlockPos pos) {
        if (MeteorClient.mc.world == null) return false;

        Block centerBlock = MeteorClient.mc.world.getBlockState(pos).getBlock();

        if (!targetBlocks.contains(centerBlock)) {
            return false;
        }

        Direction[] horizontalDirections = new Direction[]{
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
        };

        int surroundingOtherTypesCount = 0;

        for (Direction dir : horizontalDirections) {
            Block neighborBlock = MeteorClient.mc.world.getBlockState(pos.offset(dir)).getBlock();

            if (targetBlocks.contains(neighborBlock) && neighborBlock != centerBlock) {
                surroundingOtherTypesCount++;
            }
        }

        return surroundingOtherTypesCount >= 3;
    }
}