package com.alpsbte.plotsystem;

import com.alpsbte.plotsystem.core.system.plot.MockPlot;
import com.alpsbte.plotsystem.utils.MockBukkitPlatform;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.*;
import java.util.function.Supplier;

@DisplayName("WorldEdit Test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlot implements PlotSystemServer {
    protected static final Supplier<BuiltInClipboardFormat> TESTING_FORMAT = () -> BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC;

    protected static ServerMock server;
    protected static WorldEditPlugin worldedit;

    @Override
    public void onServerStarted(ServerMock server, WorldEditPlugin worldedit) {
        TestPlot.server = server;
        TestPlot.worldedit = worldedit;
    }

    @AfterAll @Override
    public void onServerStop() {
        server.getLogger().info("[TEST] ==========[ PLUGIN TEST END ]============");
        PlotSystemServer.super.onServerStop();
    }

    @Test @Order(1)
    @DisplayName("Check WorldEdit")
    public void checkPlatform() {
        PlatformManager platform = WorldEdit.getInstance().getPlatformManager();

        Assertions.assertTrue(platform.isInitialized());

        Assertions.assertEquals(1,
            platform.getPlatforms().size()
        , "Should only have one registered platform.");
        Assertions.assertInstanceOf(
            MockBukkitPlatform.class,
            platform.getPlatforms().getFirst()
        , "Platform should be created from our mocked class.");

        Assertions.assertNotNull(BlockTypes.AIR);
        Assertions.assertNotNull(BlockTypes.DIAMOND_BLOCK);
    }

    @Test @Order(2)
    @DisplayName("Test Plot")
    public void testPlot() throws IOException {
        WorldMock world = new WorldMock(Material.STONE, 16);
        world.setName("mock");

        server.addWorld(world);
        PlayerMock player = server.addPlayer("John_PlotSystem");


        System.out.println(server.getWorlds().stream().map(WorldInfo::getName).toList());

        // MockAbstractPlot plot = new MockAbstractPlot(0, player.getUniqueId());

        byte[] initialSchematic = Assertions.assertDoesNotThrow(() -> mockInitialSchematic(player));

        MockPlot plot = new MockPlot(initialSchematic, initialSchematic, "0,0|15,0|15,15|0,15", player);

        System.out.println(worldedit.getDataFolder().toPath().toString());

        // Generate stone for 16 blocks

        System.out.println(server.getName());
        System.out.println("World: " + plot.getWorld().getPlotHeightCentered());
        System.out.println("Center: " + plot.getCenter());
    }

    public static byte[] mockInitialSchematic(@NotNull PlayerMock player) throws RuntimeException, WorldEditException, IOException {
        try(EditSession edits = worldedit.createEditSession(player)) {
            com.sk89q.worldedit.world.World editWorld = edits.getWorld();
            BlockVector3 center = BlockVector3.ONE;
            int radiusX = 50;
            int radiusZ = 50;
            int minY = 1;
            int maxY = 128 - 1;

            Region region = new CylinderRegion(editWorld, center, new Vector2(radiusX, radiusZ), minY, maxY);

            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            Clipboard clipboard = new BlockArrayClipboard(region);
            ForwardExtentCopy copy = new ForwardExtentCopy(edits, region, clipboard, min);
            Operations.complete(copy);

            try(ByteArrayOutputStream initialSchematic = new ByteArrayOutputStream()) {
                try(ClipboardWriter writer = TESTING_FORMAT.get().getWriter(initialSchematic)) {
                    writer.write(clipboard);
                }
                return initialSchematic.toByteArray();
            }
        }
    }
}
