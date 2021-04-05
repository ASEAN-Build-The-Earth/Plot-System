package github.BTEPlotSystem.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SpecialBlocks {
    public static ItemStack SeamlessSandstone = new ItemBuilder(Material.SANDSTONE, 1, (byte) 2)
            .setName("§6§lSeamless Sandstone").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §fSeamless Sandstone§7 block.")
                .emptyLine()
                .build())
            .setEnchantment(true)
            .build();

    public static ItemStack SeamlessStone = new ItemBuilder(Material.STONE, 1)
            .setName("§6§lSeamless Stone").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §fSeamless Stone§7 block.")
                .emptyLine()
                .build())
            .setEnchantment(true)
            .build();

    public static ItemStack RedMushroom = new ItemBuilder(Material.HUGE_MUSHROOM_2, 1)
            .setName("§6§lRed Mushroom").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §fRed Mushroom§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack BrownMushroom = new ItemBuilder(Material.HUGE_MUSHROOM_1, 1)
            .setName("§6§lBrown Mushroom Block").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §fBrown Mushroom§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack MushroomStem = new ItemBuilder(Material.HUGE_MUSHROOM_2, 1)
            .setName("§6§lMushroom Stem Block").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §fMushroom Stem§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack LightBrownMushroom = new ItemBuilder(Material.HUGE_MUSHROOM_1, 1)
            .setName("§6§lLight Brown Mushroom Block").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §fLight Brown Mushroom§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack Barrier = new ItemBuilder(Material.BARRIER, 1)
            .setName("§6§lBarrier").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place an §fInvisible§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack BarkOakLog = new ItemBuilder(Material.LOG, 1, (byte) 0)
            .setName("§6§lBark Oak Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §f6-Sided Bark Oak Log§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack BarkBirchLog = new ItemBuilder(Material.LOG, 1, (byte) 2)
            .setName("§6§lBark Birch Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §f6-Sided Bark Birch Log§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack BarkSpruceLog = new ItemBuilder(Material.LOG, 1, (byte) 1)
            .setName("§6§lBark Spruce Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §f6-Sided Bark Spruce Log§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack BarkJungleLog = new ItemBuilder(Material.LOG, 1, (byte) 3)
            .setName("§6§lBark Jungle Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §f6-Sided Bark Jungle Log§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack BarkAcaciaLog = new ItemBuilder(Material.LOG_2, 1, (byte) 0)
            .setName("§6§lBark Acacia Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §f6-Sided Bark Acacia Log§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();

    public static ItemStack BarkDarkOakLog = new ItemBuilder(Material.LOG_2, 1, (byte) 1)
            .setName("§6§lBark Dark Oak Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLine("Use this tool to place a §f6-Sided Bark Dark Oak Log§7 block.")
                .emptyLine()
            .build())
            .setEnchantment(true)
            .build();
}