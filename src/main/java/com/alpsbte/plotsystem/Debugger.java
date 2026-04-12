package com.alpsbte.plotsystem;

import com.fastasyncworldedit.core.extent.clipboard.CPUOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.DiskOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.io.FastSchematicReaderV2;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public interface Debugger {

    default void debugClipboard(@NotNull Clipboard clipboard,
                                @NotNull String from) {
        if(clipboard instanceof BlockArrayClipboard faweClipboard) {
            PlotSystem.getPlugin().getComponentLogger().info(
                text(from + " called on clipboard '" + faweClipboard.getParent().getClass().getName() + "'")
            );
            PlotSystem.getPlugin().getComponentLogger().info(
                text("clipboard size '" + faweClipboard.getDimensions() + "'")
            );
            PlotSystem.getPlugin().getComponentLogger().info(
                text("clipboard volume '" + faweClipboard.getVolume() + "'")
            );
            PlotSystem.getPlugin().getComponentLogger().info(
                text("clipboard area '" + faweClipboard.getMinimumPoint() + "' to '" + faweClipboard.getMaximumPoint() + "'")
            );

            if(faweClipboard.getParent() instanceof DiskOptimizedClipboard disk) {
                java.net.URI uri = disk.getURI();

                if (!"file".equalsIgnoreCase(uri.getScheme())) {
                    throw new IllegalArgumentException("Not a file URI");
                }
                try {
                    Path path = Paths.get(uri);
                    long bytes = Files.size(path);

                    double mb = bytes / (1024.0 * 1024.0);

                    PlotSystem.getPlugin().getComponentLogger().info(
                            text("clipboard file size " + mb + "MB")
                    );
                }
                catch (IOException ex) {
                    PlotSystem.getPlugin().getComponentLogger().error(
                            text("cannot analize clipboard file size")
                            , ex);
                }
            }
        }
        else {
            PlotSystem.getPlugin().getComponentLogger().warn(
                text("Unknown clipboard to debug '" + clipboard.getClass().getName() + "'")
            );
        }
    }

}
