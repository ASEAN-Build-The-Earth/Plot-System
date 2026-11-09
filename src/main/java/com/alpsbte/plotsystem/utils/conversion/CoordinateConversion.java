package com.alpsbte.plotsystem.utils.conversion;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.conversion.projection.GeographicProjection;
import com.alpsbte.plotsystem.utils.conversion.projection.OffsetProjectionTransform;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.alpsbte.plotsystem.utils.conversion.projection.ScaleProjectionTransform;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CoordinateConversion {

    private static GeographicProjection projection;

    private static final DecimalFormat decFormat1 = new DecimalFormat();

    static {
        decFormat1.setMaximumFractionDigits(1);
        DecimalFormatSymbols usSymbols = new DecimalFormatSymbols(Locale.US);
        decFormat1.setDecimalFormatSymbols(usSymbols);
        double[] terraOffset = getTerraOffset();

        projection = GeographicProjection.projections.get("bteairocean");
        projection = GeographicProjection.orientProjection(projection, GeographicProjection.Orientation.upright);
        projection = new ScaleProjectionTransform(projection, 7318261.522857145, 7318261.522857145);
        projection = new OffsetProjectionTransform(projection, terraOffset[0], terraOffset[1]);
    }

    /**
     * Get the configured Terra-Server coordinates offset from Plot-System plugin.
     * @return the offset as double array {x,z}, {0,0} if not configured.
     */
    public static double[] getTerraOffset() {
        // Plot-System does not exist, this class is initializing before its enabled.
        if(PlotSystem.getPlugin() == null) return new double[]{0, 0};

        String configOffsetX = PlotSystem.getPlugin().getConfig().getString(ConfigPaths.TERRA_OFFSET_X, null);
        String configOffsetZ = PlotSystem.getPlugin().getConfig().getString(ConfigPaths.TERRA_OFFSET_Z, null);

        // Config does not exist, possibly old config version
        if(configOffsetX == null || configOffsetZ == null)  return new double[]{0, 0};

        try {
            return new double[] {
                Double.parseDouble(configOffsetX),
                Double.parseDouble(configOffsetZ)
            };
        }
        catch (NumberFormatException ex) {
            PlotSystem.getPlugin().getComponentLogger().error("Plot-System failed to parse configured terra coordinates offset.");
            return new double[]{0, 0};
        }
    }

    /**
     * Converts Minecraft coordinates to geographic coordinates
     *
     * @param xCords - Minecraft player x-axis coordinates
     * @param yCords - Minecraft player y-axis coordinates
     * @return - WG84 EPSG:4979 coordinates as double array {lon,lat} in degrees
     */
    public static double[] convertToGeo(double xCords, double yCords) throws OutOfProjectionBoundsException {
        return projection.toGeo(xCords, yCords);
    }

    /**
     * Gets in-game coordinates from geographical location
     *
     * @param lon Geographical Longitude
     * @param lat Geographic Latitude
     * @return The in-game coordinates (x, z)
     */
    public static double[] convertFromGeo(double lon, double lat) throws OutOfProjectionBoundsException {
        return projection.fromGeo(lon, lat);
    }
}
