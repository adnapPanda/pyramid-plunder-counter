package com.pyramidplundercounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("PyramidPlunderCountConfig")
public interface PyramidPlunderCounterConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "showChestsLooted",
			name = "Chests Looted",
			description = "Displays the number of chests looted"
	)
	default boolean showChestsLooted() {
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "showSarcoLooted",
			name = "Sarcophagus Looted",
			description = "Displays the number of sarcophagus looted"
	)
	default boolean showSarcoLooted() {
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "showChance",
			name = "% Chance of having received at least one sceptre",
			description = "Displays the percentage chance of having received at least one sceptre."
	)
	default boolean showChance() {
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "showPetChance",
			name = "% Chance of having received pet",
			description = "Displays the percentage chance of having received at least one pet."
	)
	default boolean showPetChance() {
		return true;
	}

	@ConfigItem(
			position = 4,
			keyName = "saveData",
			name = "Save your data",
			description = "Save your data cross-sessions to keep track of it."
	)

	default boolean saveData() {
		return true;
	}
}
