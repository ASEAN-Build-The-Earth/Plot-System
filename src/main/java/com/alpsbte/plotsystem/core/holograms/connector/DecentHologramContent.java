package com.alpsbte.plotsystem.core.holograms.connector;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


public interface DecentHologramContent {
    ItemStack getItem();

    String getTitle(UUID var1);

    List<DecentHologramDisplay.DataLine<?>> getHeader(UUID var1);

    List<DecentHologramDisplay.DataLine<?>> getContent(UUID var1);

    List<DecentHologramDisplay.DataLine<?>> getFooter(UUID var1);
}