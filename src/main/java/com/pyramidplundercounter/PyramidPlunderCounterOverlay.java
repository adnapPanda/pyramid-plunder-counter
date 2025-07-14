package com.pyramidplundercounter;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;

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
        addMenuEntry(RUNELITE_OVERLAY, "Reset", "Chests Looted");
        addMenuEntry(RUNELITE_OVERLAY, "Reset", "Sarcophagus Looted");
        addMenuEntry(RUNELITE_OVERLAY, "Reset", "Chests since last sceptre");
        addMenuEntry(RUNELITE_OVERLAY, "Reset", "Sarcos since last sceptre");
        addMenuEntry(RUNELITE_OVERLAY, "Reset", "Sceptre Chance");
        addMenuEntry(RUNELITE_OVERLAY, "Reset", "Pet Chance");
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

            if (config.showChestsSinceLastSceptre())
                elems.add(LineComponent.builder()
                        .left("Chests Since Last Sceptre:")
                        .right(String.format("%d", plugin.chestSinceLastSceptre))
                        .build());

            if (config.showSarcosSinceLastSceptre())
                elems.add(LineComponent.builder()
                        .left("Sarcos Since Last Sceptre:")
                        .right(String.format("%d", plugin.sarcoSinceLastSceptre))
                        .build());

            if (config.showChance())
                elems.add(LineComponent.builder()
                        .left("% Chance of at least one Sceptre:")
                        .right(String.format("%f", plugin.dryChance*100))
                        .build());

            if (config.showPetChance())
                elems.add(LineComponent.builder()
                        .left("% Chance of pet:")
                        .right(String.format("%f", plugin.petDryChance*100))
                        .build());

            if (config.showResetTooltip())
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("\nShift right-click this box to reset specific totals.")
                        .build());
        }
        return super.render(graphics);
    }
}
