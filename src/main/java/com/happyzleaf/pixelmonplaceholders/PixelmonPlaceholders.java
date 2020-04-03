package com.happyzleaf.pixelmonplaceholders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = PixelmonPlaceholders.PLUGIN_ID,
		name = PixelmonPlaceholders.PLUGIN_NAME,
		version = PixelmonPlaceholders.VERSION,
		authors = "happyzleaf",
		url = "https://happyzleaf.com/",
		dependencies = {
				@Dependency(id = "pixelmon", version = "7.2.2"),
				@Dependency(id = "placeholderapi", version = "[4.4,)")
		})
public class PixelmonPlaceholders {
	public static final String PLUGIN_ID = "pixelmonplaceholders";
	public static final String PLUGIN_NAME = "PixelmonPlaceholders";
	public static final String VERSION = "3.0.0";

	public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);

	@Listener
	public void init(GameInitializationEvent event) {
		if (!new Placeholders().register(this)) {
			return;
		}

		LOGGER.info("%s v%s loaded! Proudly made by happyz. (https://happyzleaf.com/)");
	}
}
