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
			description = "Displays the number of chests successfully looted"
	)
	default boolean showChestsLooted() {
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "showSarcoLooted",
			name = "Sarcophagus Looted",
			description = "Displays the number of sarcophagus successfully looted"
	)
	default boolean showSarcoLooted() {
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "showTotalChestsLooted",
			name = "Total Chests Looted",
			description = "Displays the number of total chests looted. This includes both successful and unsuccessful attempts."
	)
	default boolean showTotalChestsLooted() {
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "showTotalSarcoLooted",
			name = "Total Sarcophagus Looted",
			description = "Displays the number of total Sarcophagus looted. This includes both successful and unsuccessful attempts."
	)
	default boolean showTotalSarcoLooted() {
		return true;
	}
}
