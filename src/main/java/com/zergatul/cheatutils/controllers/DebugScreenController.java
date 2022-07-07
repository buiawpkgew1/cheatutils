package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class DebugScreenController {

    public static final DebugScreenController instance = new DebugScreenController();

    private final Minecraft mc = Minecraft.getInstance();
    private final DecimalFormat format = new DecimalFormat("0.00");

    private DebugScreenController() {

    }

    public void onGetGameInformation(List<String> list) {
        list.add("");
        list.add("Zergatul Cheat Utils");
        list.add("Loaded chunks: " + ChunkController.instance.getLoadedChunksCount());
        list.add("Block scanning thread queue size: " + BlockFinderController.instance.getScanningQueueCount());
        list.add("Block scanning thread load: " + format.format(BlockFinderController.instance.getScanningThreadLoadPercent()) + "%");
        list.add("Horizontal speed: " + format.format(SpeedCounterController.instance.getSpeed()));

        FreeCamController.instance.onDebugScreenGetGameInformation(list);
    }

    public void onGetSystemInformation(List<String> list) {
        FreeCamController.instance.onDebugScreenGetSystemInformation(list);
    }
}