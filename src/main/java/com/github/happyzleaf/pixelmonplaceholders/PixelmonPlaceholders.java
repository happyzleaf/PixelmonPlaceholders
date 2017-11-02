package com.github.happyzleaf.pixelmonplaceholders;

import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.lang.reflect.Field;

/***************************************
 * PixelmonPlaceholders
 * Created on 29/05/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
@Plugin(id = PixelmonPlaceholders.PLUGIN_ID, name = PixelmonPlaceholders.PLUGIN_NAME, description = "Pixelmon Placeholders", version = PixelmonPlaceholders.VERSION,
		url = "https://github.com/happyzleaf/PixelmonPlaceholders", authors = {"happyzleaf"}, dependencies = {@Dependency(id = "pixelmon"), @Dependency(id = "placeholderapi", version = "[4.1,)")})
public class PixelmonPlaceholders {
	public static final String PLUGIN_ID = "zpixelmonplaceholders";
	public static final String PLUGIN_NAME = "PixelmonPlaceholders";
	public static final String VERSION = "1.2.0";
	
	public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);
	
	@Listener
	public void onServerStart(GameStartingServerEvent event) {
		LOGGER.info("Preventing PlaceholderAPI from spamming.");
		try {
			Field logger = PlaceholderAPIPlugin.class.getDeclaredField("logger");
			logger.setAccessible(true);
			logger.set(PlaceholderAPIPlugin.getInstance(), new FakeLogger());
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
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
		}).map(builder -> builder.author("happyzlife").version(VERSION)).forEach(builder -> {
			try {
				builder.buildAndRegister();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		
		/*LOGGER.info("Registering Seen Counter");
		Pixelmon.EVENT_BUS.register(new SeenEvent());*/
		
		LOGGER.info("Loaded! This plugin was made by happyzleaf :)");
	}
}
