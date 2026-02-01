package com.alpsbte.plotsystem;

/**
 * Skip everything just so {@link #getPlugin()} returns non-null value.
 */
public class MockPlotSystemPlugin extends PlotSystem {
    @Override
    public void onEnable() {
        plugin = this;
        pluginEnabled = true;
    }
}
