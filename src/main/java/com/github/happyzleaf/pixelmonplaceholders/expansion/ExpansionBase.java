package com.github.happyzleaf.pixelmonplaceholders.expansion;

import com.github.happyzleaf.pixelmonplaceholders.PixelmonPlaceholders;
import me.rojo8399.placeholderapi.expansions.Expansion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;

/***************************************
 * PixelmonPlaceholders
 * Created on 29/05/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public abstract class ExpansionBase implements Expansion {
	@Override
	public boolean canRegister() {
		return Sponge.getPluginManager().isLoaded("pixelmon");
	}
	
	@Override
	public String getAuthor() {
		return "happyzleaf";
	}
	
	@Override
	public String getVersion() {
		return PixelmonPlaceholders.VERSION;
	}
	
	@Override
	public abstract String getDescription();
	
	@Override
	public abstract List<String> getSupportedTokens();
	
	@Override
	public abstract Object onValueRequest(Player player, Optional<String> token);
	
	@Override
	public Text onPlaceholderRequest(Player player, Optional<String> optional) {
		Object val = onValueRequest(player, optional);
		if (val == null) {
			return null;
		} else if (val instanceof Text) {
			return (Text) val;
		} else if (val instanceof String) {
			return TextSerializers.FORMATTING_CODE.deserialize((String) val);
		} else {
			return Text.of(val);
		}
	}
}
