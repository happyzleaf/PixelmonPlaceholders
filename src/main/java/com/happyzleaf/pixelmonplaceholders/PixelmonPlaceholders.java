package com.happyzleaf.pixelmonplaceholders;

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
}
