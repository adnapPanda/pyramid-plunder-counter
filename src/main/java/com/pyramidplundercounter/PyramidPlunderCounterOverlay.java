package com.pyramidplundercounter;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

class PyramidPlunderCounterOverlay extends OverlayPanel {

    private Client client;
    private PyramidPlunderCounterPlugin plugin;
    private PyramidPlunderCounterConfig config;

    @Inject
    PyramidPlunderCounterOverlay(PyramidPlunderCounterPlugin plugin, Client client, PyramidPlunderCounterConfig config) {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.isInPyramidPlunder()) {
            List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
            elems.clear();
            panelComponent.setPreferredSize(new Dimension(200, 100));
            if (config.showChestsLooted())
                elems.add(LineComponent.builder()
                        .left("Total Chests Looted:")
                        .right(String.format("%d", plugin.chestLooted))
                        .build());

            if (config.showSarcoLooted())
                elems.add(LineComponent.builder()
                        .left("Total Sarcophagi Looted:")
                        .right(String.format("%d", plugin.sarcoLooted))
                        .build());

            if (config.showChance())
                elems.add(LineComponent.builder()
                        .left("% Chance of at least one Sceptre:")
                        .right(String.format("%f", plugin.dryChance*100))
                        .build());
        }
        return super.render(graphics);
    }
}
