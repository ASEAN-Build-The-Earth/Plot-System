package com.alpsbte.plotsystem;

import com.fastasyncworldedit.core.extent.clipboard.DiskOptimizedClipboard;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
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
        Class<? extends Clipboard> parent =
            (clipboard.getClass() == BlockArrayClipboard.class)?
            ((BlockArrayClipboard) clipboard).getParent().getClass() : clipboard.getClass();

        PlotSystem.getPlugin().getComponentLogger().info(
            text(from + " called on clipboard '" + parent + "'")
        );
        PlotSystem.getPlugin().getComponentLogger().info(
            text("clipboard size '" + clipboard.getDimensions() + "'")
        );
        PlotSystem.getPlugin().getComponentLogger().info(
            text("clipboard volume '" + clipboard.getVolume() + "'")
        );
        PlotSystem.getPlugin().getComponentLogger().info(
            text("clipboard area '" + clipboard.getMinimumPoint() + "' to '" + clipboard.getMaximumPoint() + "'")
        );

        if(parent == DiskOptimizedClipboard.class) {
            java.net.URI uri = clipboard.getURI();

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

}
