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
	
	private static String moveNotAvailableMessage = "Attack not available.";
	public static Text moveNotAvailableText;
	
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
		disabledEggText = deserialize(disabledEggMessage);
		
		entityNotFoundMessage = miscellaneous.getNode("entityNotFoundMessage").getString();
		entityNotFoundText = deserialize(entityNotFoundMessage);
		
		evolutionNotAvailableMessage = miscellaneous.getNode("evolutionNotAvailableMessage").getString();
		evolutionNotAvailableText = deserialize(evolutionNotAvailableMessage);
		
		moveNotAvailableMessage = miscellaneous.getNode("moveNotAvailableMessage").getString();
		moveNotAvailableText = deserialize(moveNotAvailableMessage);
	}
	
	public static void saveConfig() {
		load();
		
		CommentedConfigurationNode miscellaneous = node.getNode("miscellaneous");
		
		miscellaneous.getNode("disableEggInfo").setValue(disableEggInfo);
		miscellaneous.getNode("disabledEggMessage").setValue(disabledEggMessage);
		
		miscellaneous.getNode("entityNotFoundMessage").setValue(entityNotFoundMessage);
		
		miscellaneous.getNode("evolutionNotAvailableMessage").setValue(evolutionNotAvailableMessage);
		
		miscellaneous.getNode("moveNotAvailableMessage").setValue(moveNotAvailableMessage);
		
		save();
	}
	
	private static Text deserialize(String message) {
		return TextSerializers.FORMATTING_CODE.deserialize(message);
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
