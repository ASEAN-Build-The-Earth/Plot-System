/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.alpslib.utils.AlpsUtils;
// import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import com.aseanbte.aseanlib.hologram.DecentHologramDisplay;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
//import me.filoghost.holographicdisplays.api.Position;
//import me.filoghost.holographicdisplays.api.hologram.Hologram;
//import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public abstract class AbstractTutorialHologram extends DecentHologramDisplay {
    protected static final String READ_EMOJI = "✅";
    private final static int MAX_HOLOGRAM_LENGTH = 48; // The maximum length of a line in the hologram
    private final static String HOLOGRAM_LINE_BREAKER = "%newline%";
    private final static String EMPTY_TAG = "&f";

    protected final Player player;
    protected final int holoId;
    protected final String content;
    protected final int readMoreId;

    protected final Tutorial tutorial;

    private final Vector vectorPos;
    private ClickAction markAsReadClickAction;

    public AbstractTutorialHologram(Player player, int holoId, String content, int readMoreId) {
        super(String.valueOf(holoId), null, true);
        this.holoId = holoId;
        this.player = player;
        this.content = content;
        this.readMoreId = readMoreId;

        tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
        vectorPos = TutorialUtils.getTipPoints(tutorial.getConfig()).get(holoId);
    }

    /**
     * Gets the hologram title in the header
     * @return formatted title
     */
    protected abstract String getTitle();

    /**
     * The text which is displayed on the hologram to read more.
     * @return formatted read more text
     */
    protected abstract String getReadMoreActionText();

    /**
     * This method gets executed after the player has clicked on the 'read more' text on the hologram.
     */
    protected abstract void handleReadMoreClickAction();

    /**
     * The text which is displayed on the hologram to mark it as read.
     * @return formatted text
     */
    protected abstract String getMarkAsReadActionText();

    /**
     * The text which is displayed after the player marked the hologram as read.
     * @return formatted text
     */
    protected abstract String getMarkAsReadActionDoneText();

    @Override
    public void create(Player player) {
        setLocation(new Location(player.getWorld(), vectorPos.getX(), vectorPos.getY(), vectorPos.getZ()));
        super.create(player);
    }

    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return getTitle();
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return player.getUniqueId().toString().equals(uuid.toString());
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Collections.singletonList(new TextLine(this.getTitle(playerUUID)));
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        List<DataLine<?>> lines = new ArrayList<>();
        List<String> innerLines = AlpsUtils.createMultilineFromString(content, MAX_HOLOGRAM_LENGTH, HOLOGRAM_LINE_BREAKER);
        innerLines.forEach(innerLine -> lines.add(new TextLine(innerLine)));
        return lines;
    }

    @Override
    public List<DataLine<?>> getFooter(UUID playerUUID) {
        List<DataLine<?>> lines = new ArrayList<>();
        lines.add(new TextLine(EMPTY_TAG));
        if (readMoreId != -1) lines.add(new TextLine(getReadMoreActionText()));
        if (markAsReadClickAction != null) {
            if (readMoreId != -1) lines.add(new TextLine(EMPTY_TAG));
            lines.add(new TextLine(getMarkAsReadActionText()));
        }
        return lines;
    }

    public void setMarkAsReadClickAction(ClickAction action) {
        markAsReadClickAction = action;
    }

    @Override
    public void reload(UUID playerUUID) {
        super.reload(playerUUID);

        // Set click listener
        Hologram holo = this.getHologram(playerUUID);

        if (readMoreId != -1)  super.setClickListener((clickEvent) -> handleReadMoreClickAction());
        if (markAsReadClickAction != null) {
            HologramLine line = holo.getPage(0).getLines().get(holo.getPage(0).getLines().size() - 1);
            super.setClickListener((clickEvent) -> {
                line.setText(getMarkAsReadActionDoneText());
                markAsReadClickAction.onClick(clickEvent);
            });
        }
    }

    protected String getReadMoreLink() {
        return TutorialUtils.getDocumentationLinks(tutorial.getConfig()).get(readMoreId);
    }
}
