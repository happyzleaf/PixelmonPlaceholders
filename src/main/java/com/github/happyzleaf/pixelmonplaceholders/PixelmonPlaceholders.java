package com.github.happyzleaf.pixelmonplaceholders;

import com.google.inject.Inject;
import me.rojo8399.placeholderapi.PlaceholderService;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;

/***************************************
 * PixelmonPlaceholders
 * Created on 29/05/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
@Plugin(id = PixelmonPlaceholders.PLUGIN_ID, name = PixelmonPlaceholders.PLUGIN_NAME, description = "Pixelmon Placeholders", version = PixelmonPlaceholders.VERSION,
		url = "https://github.com/happyzleaf/PixelmonPlaceholders", authors = {"happyzleaf"}, dependencies = {@Dependency(id = "pixelmon"), @Dependency(id = "placeholderapi", version = "[4.2,)")})
public class PixelmonPlaceholders {
	public static final String PLUGIN_ID = "zpixelmonplaceholders";
	public static final String PLUGIN_NAME = "PixelmonPlaceholders";
	public static final String VERSION = "1.3.0";
	
	public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	
	@Inject
	@DefaultConfig(sharedRoot = true)
	private File configFile;
	
	@Listener
	public void preInit(GamePreInitializationEvent event) {
		PPConfig.init(configLoader, configFile);
	}
	
	@Listener
	public void onServerStart(GameStartingServerEvent event) {
		LOGGER.info("Registering Pixelmon Placeholders.");
		
		Sponge.getServiceManager().provideUnchecked(PlaceholderService.class).loadAll(new Placeholders(), this).stream().map(builder -> {
			switch (builder.getId()) {
				case "trainer":
					return builder.tokens("dexcount", "dexpercentage", "seencount", "wins", "losses", "wlratio", "balance", "team-[position]").description("Pixelmon trainer's Placeholders.");
				case "pixelmon":
					return builder.tokens("dexsize", "dexsizeall").description("General Pixelmon's placeholders.");
				case "pokedex":
					return builder.tokens("[name]", "[nationalId]").description("Specific Pokémon's placeholders.");
			}
			return builder;
		}).map(builder -> builder.author("happyzleaf").plugin(this).version(VERSION)).forEach(builder -> {
			try {
				builder.buildAndRegister();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		
		LOGGER.info("Loaded! This plugin was made by happyzleaf :)");
	}
	
	@Listener
	public void onGameReload(GameReloadEvent event) {
		PPConfig.loadConfig();
		LOGGER.info("Reloaded.");
	}
}
