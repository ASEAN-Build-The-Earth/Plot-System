package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.core.database.DataProviderOperationException;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Material;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class MockPlot extends Plot {
    protected byte[] initialSchematicBytes;
    protected byte[] completedSchematicBytes;
    protected static final Random RNG = new Random();

    /**
     * Matches {@code float,float}
     */
    protected static final String FLOAT_FLOAT = "-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)";

    /**
     * Matches {@code float,float|float,float|...}
     */
    protected static final String OUTLINE_PATTERN = '^' + FLOAT_FLOAT + "?(\\|" + FLOAT_FLOAT + "?)*$";

    protected static Country mockCountry = new Country(
            "MOCK", Continent.ASIA, Material.DIAMOND_BLOCK.name(), null) {

        @Override
        public boolean setMaterialAndModelData(String material, @Nullable String customModelData) {
            throw DataProviderOperationException.get();
        }

        @Override
        public List<CityProject> getCityProjects() {
            throw DataProviderOperationException.get();
        }
    };

    protected static CityProject mockProject = new CityProject(
            "TEST", "PS", "ServerMock", true, 1) {

        @Override
        public Country getCountry() {
            return mockCountry;
        }

        @Override
        public boolean setBuildTeam(int buildTeamId) {
            throw DataProviderOperationException.get();
        }

        @Override
        public boolean setServer(String serverName) {
            throw DataProviderOperationException.get();
        }

        @Override
        public boolean setVisible(boolean isVisible) {
            throw DataProviderOperationException.get();
        }

        @Override
        public BuildTeam getBuildTeam() {
            throw DataProviderOperationException.get();
        }
    };

    public MockPlot(byte[] initialSchematicBytes,
                    byte[] completedSchematicBytes,
                    @Pattern(OUTLINE_PATTERN) String outlineBounds,
                    @NotNull PlayerMock owner) {
       this(
           initialSchematicBytes,
           completedSchematicBytes,
           RNG.nextInt(32),
           mockProject,
           PlotDifficulty.values()[0],
           owner.getUniqueId(),
           Status.values()[0],
           outlineBounds,
           LocalDate.now(),
           3.0,
           PlotType.CITY_INSPIRATION_MODE,
           List.of()
       );
    }

    private MockPlot(// Testing Schematic
                    byte[] initialSchematicBytes,
                    byte[] completedSchematicBytes,
                    // Metadata
                    int id,
                    CityProject cityProject,
                    PlotDifficulty difficulty,
                    UUID ownerUUID,
                    Status status,
                    String outlineBounds,
                    LocalDate lastActivity,
                    double version,
                    PlotType type,
                    List<Builder> members) {
        super(id, cityProject, difficulty, ownerUUID, status, outlineBounds, lastActivity, version, type, members);
        this.initialSchematicBytes = initialSchematicBytes;
        this.completedSchematicBytes = completedSchematicBytes;
    }

    @Override
    public byte[] getInitialSchematicBytes() {
        return this.initialSchematicBytes;
    }

    @Override
    public byte[] getCompletedSchematic() {
        return this.completedSchematicBytes;
    }

    //region Unsupported
    @Override
    public List<PlotReview> getReviewHistory() {
        throw DataProviderOperationException.get();
    }

    @Override
    public Optional<PlotReview> getLatestReview() {
        throw DataProviderOperationException.get();
    }

    @Override
    public boolean addPlotMember(Builder member) {
        throw DataProviderOperationException.get();
    }

    @Override
    public boolean removePlotMember(Builder member) {
        throw DataProviderOperationException.get();
    }

    @Override
    public boolean setLastActivity(boolean setNull) {
        throw DataProviderOperationException.get();
    }

    @Override
    public boolean setPasted(boolean pasted) {
        throw DataProviderOperationException.get();
    }

    @Override
    public boolean setPlotOwner(@Nullable Builder plotOwner) {
        throw DataProviderOperationException.get();
    }

    @Override
    public boolean setPlotType(PlotType type) {
        throw DataProviderOperationException.get();
    }

    @Override
    public boolean setStatus(@NotNull Status status) {
        throw DataProviderOperationException.get();
    }
    //endregion
}
