package com.alpsbte.plotsystem;

import com.google.common.cache.CacheBuilder;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.jetbrains.annotations.NotNull;

import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class Debugging implements Debugger {

    private final AtomicInteger calls = new AtomicInteger();

    public int getCalls() {
        return calls.get();
    }

    public void resetCalls() {
        calls.set(0);
    }

    @Override
    public void debugClipboard(
            @NotNull Clipboard clipboard,
            @NotNull String from
    ) {
        int count = calls.incrementAndGet();

        PlotSystem.getPlugin().getLogger().info(
            "[" + count + "] " + Instant.now()
            + " | from=" + from
            + " | size=(" + clipboard.getDimensions().x() + ", " + clipboard.getDimensions().y() + ", " + clipboard.getDimensions().z() + ')'
            + " | volume=" + clipboard.getDimensions().x() * clipboard.getDimensions().y() * clipboard.getDimensions().z()
        );

         if (count > 10_000) { // Just In-case
             Assertions.fail("Clipboard debug called >10,000 times (infinite loop)");
         }
    }
}

