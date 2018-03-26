package com.github.happyzleaf.pixelmonplaceholders;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.IOException;

public class PPConfig {
	public static boolean disableEggInfo = true;
	private static String disabledEggMessage = "&cThe eggs are disabled.";
	public static Text disabledEggText;
	private static String entityNotFoundMessage = "";
	public static Text entityNotFoundText;
	private static String evolutionNotAvailableMessage = "&cThe pokémon does not evolve with that condition.";
	public static Text evolutionNotAvailableText;
	
	private static ConfigurationLoader<CommentedConfigurationNode> loader;
	private static CommentedConfigurationNode node;
	private static File file;
	
	public static void init(ConfigurationLoader<CommentedConfigurationNode> loader, File file) {
		PPConfig.loader = loader;
		PPConfig.file = file;
		
		loadConfig();
	}
	
	public static void loadConfig() {
		if (!file.exists()) {
			saveConfig();
		}
		
		load();
		
		ConfigurationNode miscellaneous = node.getNode("miscellaneous");
		disableEggInfo = miscellaneous.getNode("disableEggInfo").getBoolean();
		disabledEggMessage = miscellaneous.getNode("disabledEggMessage").getString();
		disabledEggText = TextSerializers.FORMATTING_CODE.deserialize(disabledEggMessage);
		entityNotFoundMessage = miscellaneous.getNode("entityNotFoundMessage").getString();
		entityNotFoundText = TextSerializers.FORMATTING_CODE.deserialize(entityNotFoundMessage);
		evolutionNotAvailableMessage = miscellaneous.getNode("evolutionNotAvailableMessage").getString();
		evolutionNotAvailableText = TextSerializers.FORMATTING_CODE.deserialize(evolutionNotAvailableMessage);
	}
	
	public static void saveConfig() {
		load();
		
		CommentedConfigurationNode miscellaneous = node.getNode("miscellaneous");
		miscellaneous.getNode("disableEggInfo").setValue(disableEggInfo);
		miscellaneous.getNode("disabledEggMessage").setValue(disabledEggMessage);
		miscellaneous.getNode("entityNotFoundMessage").setValue(entityNotFoundMessage);
		miscellaneous.getNode("evolutionNotAvailableMessage").setValue(evolutionNotAvailableMessage);
		
		save();
	}
	
	private static void load() {
		try {
			node = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void save() {
		try {
			loader.save(node);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
