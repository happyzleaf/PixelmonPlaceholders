package com.github.happyzleaf.pixelmonplaceholders.expansion;

import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.pokedex.Pokedex;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/***************************************
 * PixelmonPlaceholders
 * Created on 07/06/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public class PixelmonExpansion extends ExpansionBase {
	@Override
	public String getIdentifier() {
		return "pixelmon";
	}
	
	@Override
	public String getDescription() {
		return "General Pixelmon's placeholders.";
	}
	
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList("dexsize", "dexsizeall");
	}
	
	@Override
	public Object onValueRequest(Player player, Optional<String> token) {
		if (token.isPresent()) {
			switch (token.get()) {
				case "dexsize":
					return EnumPokemon.values().length;
				case "dexsizeall":
					return Pokedex.pokedexSize;
			}
		}
		return null;
	}
}
