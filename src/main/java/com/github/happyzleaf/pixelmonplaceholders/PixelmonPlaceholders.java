package com.github.happyzleaf.pixelmonplaceholders;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;

@Plugin(id = PixelmonPlaceholders.PLUGIN_ID, name = PixelmonPlaceholders.PLUGIN_NAME, version = PixelmonPlaceholders.VERSION,
		description = "Provides placeholder's support for Reforged.",
		url = "https://www.happyzleaf.com/", authors = "happyzleaf",
		dependencies = {
				@Dependency(id = "pixelmon", version = "8.0.2"),
				@Dependency(id = "placeholderapi", version = "[4.4,)"),
				@Dependency(id = "entity-particles", version = "2.1", optional = true)
		})
public class PixelmonPlaceholders {
	public static final String PLUGIN_ID = "pixelmonplaceholders";
	public static final String PLUGIN_NAME = "PixelmonPlaceholders";
	public static final String VERSION = "2.1.5-hotfix";
	
	public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);
	
	public static PixelmonPlaceholders instance;
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private File configFile;
	
	@Listener
	public void preInit(GamePreInitializationEvent event) {
		if (Sponge.getPluginManager().isLoaded("placeholderapi")) {
			instance = this;
			PPConfig.init(configLoader, configFile);
		} else {
			LOGGER.error("PlaceholderAPI couldn't be found. The plugin will not work.");
		}
	}
	
	@Listener
	public void postInit(GamePostInitializationEvent event) {
		if (Sponge.getPluginManager().isLoaded("placeholderapi")) {
			Placeholders.register();
			LOGGER.info("Loaded! This plugin was made by happyzleaf. (https://happyzleaf.com)");
		}
	}
	
	@Listener
	public void reload(GameReloadEvent event) {
		if (Sponge.getPluginManager().isLoaded("placeholderapi")) {
			PPConfig.loadConfig();
			LOGGER.info("Reloaded.");
		}
	}
}
