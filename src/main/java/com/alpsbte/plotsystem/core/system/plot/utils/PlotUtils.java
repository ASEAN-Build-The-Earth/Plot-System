package com.alpsbte.plotsystem.core.system.plot.utils;

import com.alpsbte.alpslib.utils.MapLink;
import com.alpsbte.alpslib.utils.MapLinkEntryConfig;
import com.alpsbte.alpslib.utils.MapLinks;
import com.alpsbte.alpslib.utils.MapLinksConfig;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.plot.CMD_Plot_Members;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.core.system.review.ReviewNotification;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.DiscordUtil;
import com.alpsbte.plotsystem.utils.ShortLink;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_13;
import com.github.fierioziy.particlenativeapi.plugin.ParticleNativePlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public final class PlotUtils {
    private PlotUtils() {}

    private static final String MSG_LINE = "--------------------------";
    public static final Map<UUID, BukkitTask> plotReminder = new HashMap<>();

    /**
     * Returns the plot that the player is currently standing on or next to.
     * If he is standing in a single-plot world, it returns the plot of this world.
     * If he is standing in a multi-plot world, it returns the closest plot of all unfinished plots in this city
     *
     * @return the current plot of the player
     */
    @Nullable
    public static AbstractPlot getCurrentPlot(@NotNull Builder builder, Status... statuses) {
        boolean dev = PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE);
        if (dev) {
            PlotSystem.getPlugin().getComponentLogger().info(text(
                    "getCurrentPlot: enter | player=" + (builder.isOnline() ? builder.getPlayer().getName() : builder.getUUID()) +
                            ", statuses=" + (statuses == null ? "null" : java.util.Arrays.toString(statuses))
            ));
        }

        if (builder.isOnline()) {
            String worldName = builder.getPlayer().getWorld().getName();
            if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: worldName=" + worldName));

            if (PlotWorld.isOnePlotWorld(worldName)) {
                if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: detected OnePlotWorld"));
                try {
                    int id = Integer.parseInt(worldName.substring(2));
                    AbstractPlot plot = worldName.toLowerCase(Locale.ROOT).startsWith("p-")
                            ? DataProvider.PLOT.getPlotById(id)
                            : DataProvider.TUTORIAL_PLOT.getById(id).orElse(null);
                    if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: one-plot id=" + id + ", resolved=" + (plot == null ? "null" : (plot.getClass().getSimpleName() + "#" + plot.getId() + " status=" + plot.getStatus()))));

                    if (plot == null) return null;
                    if (statuses == null || statuses.length == 0) return plot;
                    for (Status status : statuses) if (status == plot.getStatus()) return plot;
                    if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: status filter did not match for plot #" + plot.getId()));
                    return null;
                } catch (NumberFormatException ex) {
                    if (dev) PlotSystem.getPlugin().getComponentLogger().warn(text("getCurrentPlot: failed to parse plot id from worldName=" + worldName), ex);
                    return null;
                }
            } else if (PlotWorld.isCityPlotWorld(worldName)) {
                if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: detected CityPlotWorld"));
                String cityID = worldName.substring(2);
                Optional<CityProject> city = DataProvider.CITY_PROJECT.getById(cityID);
                if (city.isEmpty()) {
                    if (dev) PlotSystem.getPlugin().getComponentLogger().warn(text("getCurrentPlot: city not found for id=" + cityID));
                    return null;
                }

                List<Plot> plots = DataProvider.PLOT.getPlots(city.get(), statuses);
                if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: candidate plots in city=" + city.get().getName(builder.isOnline() ? builder.getPlayer() : null) + ", count=" + plots.size()));
                if (plots.isEmpty()) return null;
                if (plots.size() == 1) return plots.getFirst();

                // Find the plot in the city world that is closest to the player
                Location playerLoc = builder.getPlayer().getLocation().clone();
                Vector3 playerVector = Vector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());

                double distance = Double.MAX_VALUE;
                Plot chosenPlot = plots.getFirst();
                for (Plot plot : plots) {
                    if (plot.getPlotType() != PlotType.CITY_INSPIRATION_MODE) continue;
                    BlockVector3 plotCenter = plot.getCenter();
                    double currentDistance = plotCenter.withY((int) playerVector.y()).distance(playerVector.toBlockPoint());
                    if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: candidate #" + plot.getId() + " center=" + plotCenter + " distance=" + currentDistance));
                    if (currentDistance < distance) {
                        distance = currentDistance;
                        chosenPlot = plot;
                    }
                }

                if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: chosen plot id=" + chosenPlot.getId() + ", distance=" + distance));
                return chosenPlot;
            } else {
                if (dev) PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: world is not a plot world"));
            }
        } else if (dev) {
            PlotSystem.getPlugin().getComponentLogger().info(text("getCurrentPlot: builder is offline"));
        }
        return null;
    }

    public static boolean isPlayerOnPlot(@NotNull AbstractPlot plot, Player player) {
        PlotWorld world = plot.getWorld();
        if (world.isWorldLoaded() && world.getBukkitWorld().getPlayers().contains(player)) {
            Location playerLoc = player.getLocation();
            ProtectedRegion protectedRegion = world.getProtectedRegion();
            return protectedRegion == null || protectedRegion.contains(Vector3.toBlockPoint(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()));
        }
        return false;
    }

    public static @Nullable CuboidRegion getPlotAsRegion(@NotNull AbstractPlot plot) throws IOException {
        // No longer supported!
        if (plot.getVersion() < 4) return null;

        if (!plot.getWorld().loadWorld()) return null;
        AbstractPlot.SchematicMetadata metadata = plot.getSchematicMetadata();
        return new CuboidRegion(
                metadata.minimumPoint().withY(plot.getWorld().getPlotHeight()),
                metadata.maximumPoint().withY(plot.getWorld().getBukkitWorld().getMaxHeight() - 1));
    }

    public static boolean isPlotWorld(@NotNull World world) {
        return DependencyManager.getMultiverseCore().getWorldManager().isLoadedWorld(world) && (PlotWorld.isOnePlotWorld(world.getName()) || PlotWorld.isCityPlotWorld(world.getName()));
    }

    public static byte @Nullable [] getOutlinesSchematicBytes(@NotNull AbstractPlot plot, byte[] initialSchematic) throws IOException {
        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(initialSchematic);
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }

        Polygonal2DRegion region = new Polygonal2DRegion(
                null,
                plot.getOutline(),
                clipboard.getMinimumPoint().y(),
                clipboard.getMaximumPoint().y()
        );

        BlockArrayClipboard newClipboard = new BlockArrayClipboard(region);
        // Match pasteSchematic's destination (schematic center), not the polygon centroid —
        // otherwise the outline paste is shifted and leaves voids beside the plot.
        BlockVector3 pasteOrigin = plot.getSchematicMetadata().center();
        newClipboard.setOrigin(BlockVector3.at(pasteOrigin.x(), region.getMinimumPoint().y(), pasteOrigin.z()));
        ForwardExtentCopy copy = new ForwardExtentCopy(
                clipboard,
                region,
                newClipboard,
                region.getMinimumPoint()
        );

        Operations.complete(copy);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ClipboardWriter writer = AbstractPlot.CLIPBOARD_FORMAT.getWriter(outputStream)) {
            writer.write(newClipboard);
        }
        return outputStream.toByteArray();
    }

    public static @NotNull Path getTutorialSchematicPath() {
        return Paths.get(PlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "tutorial", "schematics");
    }

    /**
     * TODO: Adapt this for the new handler so ASEAN plot shifting works.
     * @deprecated for new handler
     */
    @Deprecated
    public static boolean savePlotAsSchematic(@NotNull Plot plot) throws IOException, WorldEditException {
        if (plot.getVersion() < 4) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Saving schematics of legacy plots is no longer allowed!"));
            return false;
        }

        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(plot.getInitialSchematicBytes());
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }
        if (clipboard == null) return false;

        CuboidRegion cuboidRegion = getPlotAsRegion(plot);
        if (cuboidRegion == null) return false;

        BlockVector3 plotCenter = plot.getCenter();

        PlotSystem.getPlugin().getComponentLogger().info("Getting Plot region for saving from: {}", cuboidRegion.getCenter());

        boolean outlineShifted = isPlotOutlineShifted(plot);

        // Get plot outline
        // ASEAN START - Outline shifting
        List<BlockVector2> plotOutlines = outlineShifted? plot.getShiftedOutline() : plot.getOutline();

        // Shift schematic region to the force (0, 0) paste
        if(outlineShifted) {
            cuboidRegion.shift(BlockVector3.at(-plotCenter.x(), 0, -plotCenter.z()));

            PlotSystem.getPlugin().getComponentLogger().info("Shifted Plot region for saving to: {}", cuboidRegion.getCenter());
        }
        // ASEAN END

        // Load finished plot region as cuboid region
        if (!plot.getWorld().loadWorld()) return false;
        com.sk89q.worldedit.world.World world = new BukkitWorld(plot.getWorld().getBukkitWorld());
        Polygonal2DRegion region = new Polygonal2DRegion(null, plotOutlines, cuboidRegion.getMinimumPoint().y(), cuboidRegion.getMaximumPoint().y());

        // Copy and write finished plot clipboard to schematic
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Clipboard cb = new BlockArrayClipboard(region)) {

            // ASEAN START - Outline shifting
            // Shift the clipboard to where it is pasted in the world
            if(outlineShifted) {
                cb.setOrigin(BlockVector3.at(0, cuboidRegion.getMinimumY(), 0));
            }
            else cb.setOrigin(BlockVector3.at(plotCenter.x(), cuboidRegion.getMinimumY(), (double) plotCenter.z()));
            // ASEAN END

            // Copy the outline region to clipboard
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(world, region, cb, region.getMinimumPoint());
            Operations.complete(forwardExtentCopy);

            // Write to output stream
            try (ClipboardWriter writer = AbstractPlot.CLIPBOARD_FORMAT.getWriter(outputStream)) {
                // ASEAN START
                // Transform the outline back for saving so that the origin coordinate is the same as the original schematic
                if(outlineShifted) {
                    double initialY = clipboard.getRegion().getMinimumY();
                    double offset = initialY - cuboidRegion.getMinimumY();
                    writer.write(cb.transform(new AffineTransform().translate(Vector3.at(plotCenter.x(), offset, plotCenter.z()))));
                }
                else {
                    double initialY = clipboard.getRegion().getMinimumY();
                    double offset = initialY - cuboidRegion.getMinimumY();
                    writer.write(cb.transform(new AffineTransform().translate(Vector3.at(0, offset,0))));
                }
                // ASEAN END
            }
        }

        // Set Completed Schematic
        boolean successful = DataProvider.PLOT.setCompletedSchematic(plot.getId(), outputStream.toByteArray());
        if (!successful) return false;

        // If plot was created in a void world, copy the result to the city world
        if (plot.getPlotType() != PlotType.CITY_INSPIRATION_MODE) {
            var cpw = new CityPlotWorld(plot);
            Mask airMask = new BlockTypeMask(BukkitAdapter.adapt(cpw.getBukkitWorld()), BlockTypes.AIR);
            AbstractPlotGenerator.pasteSchematic(airMask, outputStream.toByteArray(), cpw, plot.getPlotType(), false, true);
        }
        return true;
    }

    public static @Nullable CompletableFuture<double[]> convertTerraToPlotXZ(@NotNull AbstractPlot plot, double[] terraCoords) throws IOException {
        // Calculate min and max points of schematic
        CuboidRegion plotRegion = getPlotAsRegion(plot);
        if (plotRegion == null) return null;
        AbstractPlot.SchematicMetadata metadata = plot.getSchematicMetadata();

        // Convert terra schematic coordinates into relative plot schematic coordinates
        double[] schematicCoords = {
                terraCoords[0] - metadata.minimumPoint().x(),
                terraCoords[1] - metadata.minimumPoint().z()
        };

        // Add additional plot sizes to relative plot schematic coordinates
        double[] plotCoords = {
                schematicCoords[0] + plotRegion.getMinimumPoint().x(),
                schematicCoords[1] + plotRegion.getMinimumPoint().z()
        };

        // ASEAN START
        PlotSystem.getPlugin().getComponentLogger().info(text(
                "Got TPLL Coord: " + plotCoords[0] + ", " + plotCoords[1])
        );

        // Tutorial plot does not have global terra server offsets
        if(plot instanceof TutorialPlot) {
            double[] terraOffset = CoordinateConversion.getTerraOffset();

            plotCoords[0] -= terraOffset[0];
            plotCoords[1] -= terraOffset[1];
        }

        PlotSystem.getPlugin().getComponentLogger().info(text(
                "Got TPLL Coord: " + plotCoords[0] + ", " + plotCoords[1])
        );

        // Cancel out coordinates by itself if shifted
        if(PlotUtils.isPlotOutlineShifted(plot)) {
            // Shift the plot back to original coordinates
            BlockVector3 center = plot.getBoundingBoxCenter();

            plotCoords[0] -= center.x();
            plotCoords[1] -= center.z();
        }

        PlotSystem.getPlugin().getComponentLogger().info(text(
            "Got TPLL Coord: " + plotCoords[0] + ", " + plotCoords[1])
        );
        // ASEAN END

        // Return coordinates if they are in the schematic plot region
        PlotWorld world = plot.getWorld();
        ProtectedRegion protectedPlotRegion = world.getProtectedRegion() != null
                ? world.getProtectedRegion()
                : world.getProtectedBuildRegion();
        if (protectedPlotRegion.contains(BlockVector3.at((int) plotCoords[0], world.getPlotHeightCentered(), (int) plotCoords[1]))) {
            return CompletableFuture.completedFuture(plotCoords);
        }

        return null;
    }

    // ASEAN START Plot Shifting Stradegy
    /**
     * Plot can be configured to be shifted to coordinates (0,0) upon generation; which includes: <ul>
     *     <li>Tutorial plot with version >= 2.</li>
     *     <li>Plot that shifting is explicitly enabled in the configuration.</li>
     *     <li>The plot version >= required version for shifting to be enabled.</li>
     * </ul>
     * NOTE: Shifting is skipped for plots in City Inspiration Mode.
     *
     * @param plot The plot to check.
     * @param type The plot type
     * @return {@code true} if the plot will be shifted to (0,0) upon generation.
     */
    public static boolean isPlotOutlineShifted(@NotNull AbstractPlot plot, PlotType type) {
        // City Inspiration Mode won't work with plot shifting
        return shouldShiftOutline(plot) && type != PlotType.CITY_INSPIRATION_MODE;
    }

    /**
     * @param world The plot world to check.
     * @return {@link #isPlotOutlineShifted(AbstractPlot,PlotType)}
     */
    public static boolean isPlotOutlineShifted(@NotNull PlotWorld world) {
        return isPlotOutlineShifted(world.getPlot());
    }

    /**
     * @param world The plot world to check.
     * @param type The plot type
     * @return {@link #isPlotOutlineShifted(AbstractPlot,PlotType)}
     */
    public static boolean isPlotOutlineShifted(@NotNull PlotWorld world, PlotType type) {
        return isPlotOutlineShifted(world.getPlot(), type);
    }

    /**
     *
     * @param plot The plot to check.
     * @return {@link #isPlotOutlineShifted(AbstractPlot,PlotType)}
     */
    public static boolean isPlotOutlineShifted(@NotNull AbstractPlot plot) {
        return isPlotOutlineShifted(plot, plot.getPlotType());
    }

    private static boolean shouldShiftOutline(@NotNull AbstractPlot plot) {
        boolean enabled = PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.PLOT_SHIFTING_ENABLED, false);
        int requiredVer = PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.PLOT_SHIFTING_VERSION, 4);
        double tutorialVersion = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getVersion();

        // Tutorial version >= 2 has the shifting supports.
        if(plot instanceof TutorialPlot) return tutorialVersion >= 2;

        // Requires configured setting to be enabled
        return enabled && !(plot.getVersion() < requiredVer);
    }
    // ASEAN END

    public static void checkPlotsForLastActivity() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
            List<Plot> plots = DataProvider.PLOT.getPlots(Status.unfinished);
            FileConfiguration config = PlotSystem.getPlugin().getConfig();
            long inactivityIntervalDays = config.getLong(ConfigPaths.INACTIVITY_INTERVAL, -2);
            long rejectedInactivityIntervalDays = (config.getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) != -1) ? config.getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) : inactivityIntervalDays;

            // ASEAN START
            int inactivityNotificationDays = config.getInt(ConfigPaths.INACTIVITY_NOTIFICATION_DAYS, 0);
            int inactivityNotificationTime = config.getInt(ConfigPaths.INACTIVITY_NOTIFICATION_TIME, 16);
            if (inactivityIntervalDays == -2 && rejectedInactivityIntervalDays == -2) return;

            // Determine if the current time is within the notification window.
            // Run within a ±minute window around a set local time.
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.of(inactivityNotificationTime, 0).minusMinutes(30);
            LocalTime end = LocalTime.of(inactivityNotificationTime, 0).plusMinutes(30);
            boolean inNotificationWindow = inactivityNotificationDays > 0 && !now.isBefore(start) && !now.isAfter(end);;

            for (Plot plot : plots) {
                LocalDate lastActivity = plot.getLastActivity();
                long interval = plot.isRejected() ? rejectedInactivityIntervalDays : inactivityIntervalDays;
                LocalDate abandonDate = lastActivity.plusDays(interval);

                // Check if today is within X days before the plot's abandon date
                if(inNotificationWindow && LocalDate.now().isAfter(abandonDate.minusDays(inactivityNotificationDays))) {
                    // Notify the plot's owner on discord
                    DiscordUtil.getOpt(plot.getId()).ifPresent(event -> event.onPlotInactivity(abandonDate));
                }

                if (interval == -2 || abandonDate.isAfter(LocalDate.now())) continue;

                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                    if (Actions.abandonPlot(plot, DiscordUtil.AbandonType.INACTIVE)) {
                        PlotSystem.getPlugin().getComponentLogger().info(text("Abandoned plot #" + plot.getId() + " due to inactivity!"));
                    } else {
                        PlotSystem.getPlugin().getComponentLogger().warn(text("An error occurred while abandoning plot #" + plot.getId() + " due to inactivity!"));
                    }
                });
            }
            // ASEAN END
        }, 0L, 20 * 60 * 60L); // Check every hour
    }

    public static void informPlayerAboutUnfinishedPlots(@NotNull Player player, Builder builder) {
        try {
            List<Plot> plots = Cache.getCachedInProgressPlots(builder);
            if (plots.isEmpty()) return;
            ChatFormatting.sendUnfinishedPlotReminderMessage(plots, player);
        } catch (Exception ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while trying to inform the player about his unfinished plots!"), ex);
        }
    }

    public static void startUnfinishedPlotReminderTimer(Player player) {
        int interval = PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.UNFINISHED_REMINDER_INTERVAL);
        if (interval == -1) return;
        plotReminder.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(),
                () -> informPlayerAboutUnfinishedPlots(player, Builder.byUUID(player.getUniqueId())),
                20L * 60 * interval,
                20L * 60 * interval));
    }

    public static final class Cache {
        private Cache() {}

        private static final HashMap<UUID, List<Plot>> cachedInProgressPlots = new HashMap<>();

        public static void clearCache() {
            cachedInProgressPlots.clear();
        }

        public static void clearCache(UUID builderUUID) {
            cachedInProgressPlots.remove(builderUUID);
        }

        public static List<Plot> getCachedInProgressPlots(@NotNull Builder builder) {
            if (!cachedInProgressPlots.containsKey(builder.getUUID())) {
                cachedInProgressPlots.put(builder.getUUID(), DataProvider.PLOT.getPlots(builder, Status.unfinished));
            }

            return cachedInProgressPlots.get(builder.getUUID());
        }

        public static Map<UUID, List<Plot>> getCachedInProgressPlots() {
            return cachedInProgressPlots;
        }
    }

    public static final class Effects {
        private Effects() {}

        public static final int CACHE_UPDATE_TICKS = 20 * 60;
        private static int time;

        private static boolean particleAPIEnabled = false;
        private static Particles_1_13 particles;

        public static void loadParticleNativeAPI() {
            particleAPIEnabled = DependencyManager.isParticleNativeAPIEnabled();

            // get API
            ParticleNativeAPI api = ParticleNativePlugin.getAPI();

            // choose particle list you want to use
            particles = api.getParticles_1_13();
        }

        public static void startTimer() {
            if (DependencyManager.isParticleNativeAPIEnabled())
                loadParticleNativeAPI();

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
                if (DependencyManager.isParticleNativeAPIEnabled())
                    loadParticleNativeAPI();
            }, 20 * 10L);


            Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), Effects::tick, 0L, 0L);
        }

        public static void tick() {
            time++;
            if (time % CACHE_UPDATE_TICKS == 0) Cache.clearCache();
            if (time % 10 == 0) showOutlines();
        }

        public static void showOutlines() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Builder builder = Builder.byUUID(player.getUniqueId());
                List<Plot> plots = plotsForOutlines(builder);
                if (plots.isEmpty()) continue;

                BlockVector2 playerPos2D = BlockVector2.at(player.getLocation().getX(), player.getLocation().getZ());
                for (Plot plot : plots) {
                    if (!plot.getWorld().getWorldName().equalsIgnoreCase(player.getWorld().getName())) {
                        continue;
                    }
                    // Focus-mode / legacy plots have no environment outlines to render
                    if (!plot.getPlotType().hasEnvironment() || plot.getVersion() <= 2) {
                        continue;
                    }

                    List<BlockVector2> points = plot.getBlockOutline();
                    for (BlockVector2 point : points) {
                        if (point.distanceSq(playerPos2D) >= 50 * 50) continue;
                        if (!particleAPIEnabled) {
                            player.spawnParticle(Particle.FLAME, point.x(), player.getLocation().getY() + 1, point.z(), 1, 0.0, 0.0, 0.0, 0);
                            continue;
                        }
                        Location loc = new Location(player.getWorld(), point.x(), player.getLocation().getY() + 1, point.z());
                        Object packet = particles.FLAME().packet(true, loc);
                        particles.sendPacket(player, packet);
                    }
                }
            }
        }

        /**
         * Unfinished plots the player owns/is a member of (building),
         * plus the unreviewed plot they are standing on (reviewing).
         */
        private static List<Plot> plotsForOutlines(@NotNull Builder builder) {
            List<Plot> plots = new ArrayList<>(Cache.getCachedInProgressPlots(builder));

            AbstractPlot current = getCurrentPlot(builder, Status.unreviewed);
            if (!(current instanceof Plot unreviewedPlot)) {
                return plots;
            }

            boolean alreadyIncluded = plots.stream().anyMatch(plot -> plot.getId() == unreviewedPlot.getId());
            if (!alreadyIncluded) {
                plots.add(unreviewedPlot);
            }
            return plots;
        }
    }

    public static final class ChatFormatting {
        private ChatFormatting() {}

        public static void sendLinkMessages(AbstractPlot plot, Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
                List<Component> linkMessages;
                try {
                    BlockVector3 mcCoordinates = plot.getCoordinates();
                    double[] geoCoordinates = CoordinateConversion.convertToGeo(mcCoordinates.x(), mcCoordinates.z());

                    MapLinksConfig mapLinksConfig = getMapLinksConfigFromPlugin();
                    MapLinks mapLinks = mapLinksConfig == null ? new MapLinks(geoCoordinates[1], geoCoordinates[0]) : new MapLinks(geoCoordinates[1], geoCoordinates[0], mapLinksConfig);

                    if (mapLinks.getAllLinks().isEmpty()) {
                        PlotSystem.getPlugin().getComponentLogger().warn(text("No map links are enabled for plot #" + plot.getId()));
                        return;
                    }

                    boolean shortLinksEnabled = PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.SHORTLINK_ENABLE);
                    linkMessages = new ArrayList<>(mapLinks.getAllLinks().size());
                    for (MapLink link : mapLinks.getAllLinks()) {
                        linkMessages.add(buildMapLinkMessage(player, link.name(), link.url(), shortLinksEnabled));
                    }
                } catch (IOException | URISyntaxException | NumberFormatException | NullPointerException | OutOfProjectionBoundsException ex) {
                    PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while creating map links!"), ex);
                    return;
                }

                // Temporary fix for bedrock players
                Component coords = null;
                try {
                    BlockVector3 mcCoordinates = plot.getCoordinates();
                    double[] geoCoordinates = CoordinateConversion.convertToGeo(mcCoordinates.x(), mcCoordinates.z());
                    DecimalFormat df = new DecimalFormat("##.#####");
                    df.setRoundingMode(RoundingMode.FLOOR);
                    String formattedCoords = df.format(geoCoordinates[1]) + ", " + df.format(geoCoordinates[0]);
                    coords = text(formattedCoords, GREEN)
                            .clickEvent(ClickEvent.copyToClipboard(formattedCoords))
                            .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_COPY_TO_CLIPBOARD), GRAY));
                } catch (IOException | OutOfProjectionBoundsException ex) {
                    PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);
                }

                player.sendMessage(text(MSG_LINE, DARK_GRAY));
                if (coords != null) player.sendMessage(text("Coords: ", GRAY).append(coords));
                for (Component linkMessage : linkMessages) {
                    player.sendMessage(linkMessage);
                }
                player.sendMessage(text(MSG_LINE, DARK_GRAY));

                if (plot instanceof Plot p) {
                    sendGroupTipMessage(p, player);
                    sendViewModeTipMessage(p, player);
                }
            });
        }

        private static @NotNull Component buildMapLinkMessage(@NotNull Player player, @NotNull String providerName, @NotNull String targetUrl,
                                                              boolean shortLinksEnabled) throws IOException, URISyntaxException {
            String providerLabel = " " + providerName + " ";
            Component message;
            String clickUrl = targetUrl;

            if (shortLinksEnabled) {
                clickUrl = ShortLink.generateShortLink(targetUrl);
                message = text("» ", DARK_GRAY)
                        .append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK,
                                GRAY, text(providerLabel, GREEN), text(clickUrl, GREEN)));
            } else {
                message = text("» ", DARK_GRAY)
                        .append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK,
                                GRAY, text(providerLabel, GREEN)));
            }

            return message
                    .clickEvent(ClickEvent.openUrl(clickUrl))
                    .hoverEvent(text(providerLabel));
        }

        private static @Nullable MapLinksConfig getMapLinksConfigFromPlugin() {
            ConfigurationSection linksRoot = PlotSystem.getPlugin().getConfig().getConfigurationSection(ConfigPaths.MAP_LINKS);
            if (linksRoot == null) {
                return null;
            }

            ConfigurationSection linksSection = linksRoot.getConfigurationSection("links");
            if (linksSection == null) {
                return null;
            }

            Map<String, MapLinkEntryConfig> links = new LinkedHashMap<>();
            for (String key : linksSection.getKeys(false)) {
                ConfigurationSection entry = linksSection.getConfigurationSection(key);
                if (entry == null) {
                    continue;
                }

                String id = entry.getString("id", key);
                String name = entry.getString("name");
                String template = entry.getString("url-template", entry.getString("urlTemplate"));
                Boolean enabled = entry.isSet("enabled") ? entry.getBoolean("enabled") : null;
                links.put(key, new MapLinkEntryConfig(id, name, template, enabled));
            }

            return links.isEmpty() ? null : new MapLinksConfig(links);
        }

        public static void sendGroupTipMessage(@NotNull Plot plot, Player player) {
            if (plot.getPlotMembers().isEmpty() && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)
                    && player.hasPermission(CMD_Plot_Members.PERMISSION)) {
                Component tc = text("» ", DARK_GRAY)
                        .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PLAY_WITH_FRIENDS), GRAY))
                        .clickEvent(ClickEvent.runCommand("/plot members " + plot.getId()))
                        .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.Plot.MEMBERS)));

                player.sendMessage(tc);
                player.sendMessage(text(MSG_LINE, DARK_GRAY));
            }
        }

        /**
         * Nudges newcomers toward Companion → Settings → view modes while they still have no accepted plots.
         */
        public static void sendViewModeTipMessage(@NotNull Plot plot, @NotNull Player player) {
            Builder builder = Builder.byUUID(player.getUniqueId());
            boolean isParticipant = plot.getPlotOwner().getUUID().equals(builder.getUUID())
                    || plot.getPlotMembers().stream().anyMatch(member -> member.getUUID().equals(builder.getUUID()));
            if (!isParticipant || hasCompletedPlots(builder)) return;

            player.sendMessage(text("» ", DARK_GRAY)
                    .append(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.VIEW_MODE_TIP), GREEN)));
            player.sendMessage(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_CHANGE_VIEW_MODE), GOLD)
                    .clickEvent(ClickEvent.runCommand("/companion"))
                    .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SETTINGS))));
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
        }

        /**
         * Sent after a builder's first accepted plot so they know Medium/Hard are available via Companion.
         */
        public static void sendDifficultyUnlockedMessage(@NotNull Player player) {
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
            player.sendMessage(text("» ", DARK_GRAY)
                    .append(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.DIFFICULTY_UNLOCKED), GREEN)));
            player.sendMessage(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SELECT_DIFFICULTY), GOLD)
                    .clickEvent(ClickEvent.runCommand("/companion"))
                    .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.PLOT_DIFFICULTY))));
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
        }

        public static void sendFeedbackMessage(@NotNull List<ReviewNotification> notifications, @NotNull Player player) {
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
            boolean mayHaveUnlockedDifficulty = false;
            for (ReviewNotification notification : notifications) {
                PlotReview review = DataProvider.REVIEW.getReview(notification.reviewId()).orElseThrow();
                Plot reviewedPlot = review.getPlot();
                player.sendMessage(text("» ", DARK_GRAY).append(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.REVIEWED_PLOT, String.valueOf(reviewedPlot.getId())), GREEN)));

                Component tc = text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_FEEDBACK), GOLD)
                        .clickEvent(ClickEvent.runCommand("/plot feedback " + reviewedPlot.getId()))
                        .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.Plot.PLOT_NAME) + " " + LangUtil.getInstance().get(player, LangPaths.Review.FEEDBACK)));
                player.sendMessage(tc);

                if (reviewedPlot.getStatus() == Status.completed) {
                    mayHaveUnlockedDifficulty = true;
                }

                DataProvider.REVIEW.removeReviewNotification(notification.reviewId(), notification.uuid());

                if (notifications.size() != notifications.indexOf(notification) + 1) {
                    player.sendMessage(empty());
                }
            }
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
            player.playSound(player.getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1, 1);

            // Offline builders receive review notices on join after the plot is already completed.
            if (mayHaveUnlockedDifficulty && countDistinctCompletedPlots(Builder.byUUID(player.getUniqueId())) == 1) {
                sendDifficultyUnlockedMessage(player);
            }
        }

        private static boolean hasCompletedPlots(@NotNull Builder builder) {
            return countDistinctCompletedPlots(builder) > 0;
        }

        private static long countDistinctCompletedPlots(@NotNull Builder builder) {
            return DataProvider.PLOT.getPlots(builder, Status.completed).stream()
                    .map(Plot::getId)
                    .distinct()
                    .count();
        }

        public static void sendUnfinishedPlotReminderMessage(@NotNull List<Plot> plots, @NotNull Player player) {
            player.sendMessage(text("» ", DARK_GRAY).append(text(LangUtil.getInstance().get(player, plots.size() <= 1 ? LangPaths.Message.Info.UNFINISHED_PLOT : LangPaths.Message.Info.UNFINISHED_PLOTS, String.valueOf(plots.size())), GREEN)));

            Component tc = text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_PLOTS), GOLD)
                    .clickEvent(ClickEvent.runCommand("/plots"))
                    .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS)));
            player.sendMessage(tc);
        }

        public static void sendUnreviewedPlotsReminderMessage(@NotNull List<Plot> plots, @NotNull Player player) {
            player.sendMessage(text("» ", DARK_GRAY).append(text(LangUtil.getInstance().get(player, plots.size() <= 1 ?
                    LangPaths.Message.Info.UNREVIEWED_PLOT :
                    LangPaths.Message.Info.UNREVIEWED_PLOTS, String.valueOf(plots.size())), GREEN)));

            Component tc = text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_OPEN_REVIEWS), GOLD)
                    .clickEvent(ClickEvent.runCommand("/review"))
                    .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS)))
                    .appendNewline();
            player.sendMessage(tc);
        }
    }
}
