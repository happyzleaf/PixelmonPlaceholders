package com.github.happyzleaf.pixelmonplaceholders.expansion;

import com.github.happyzleaf.pixelmonplaceholders.utility.ParserUtility;
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
public class PokedexExpansion extends ExpansionBase {
	@Override
	public String getIdentifier() {
		return "pokedex";
	}
	
	@Override
	public String getDescription() {
		return "Specific Pokémon's placeholders.";
	}
	
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList("[name]", "[nationalId]");
	}
	
	@Override
	public Object onValueRequest(Player player, Optional<String> token) {
		String[] values;
		if (token.isPresent() && (values = token.get().split("_")).length >= 1) {
			EnumPokemon pokemon = null;
			
			try {
				int nationalId = Integer.parseInt(values[0]);
				if (nationalId >= 0 && nationalId <= EnumPokemon.values().length) {
					pokemon = EnumPokemon.getFromNameAnyCase(Pokedex.fullPokedex.get(nationalId).name);
				}
			} catch (NumberFormatException e) {
				pokemon = EnumPokemon.getFromNameAnyCase(values[0]);
			}
			
			if (pokemon != null) {
				return ParserUtility.parsePokedexInfo(pokemon, values);
			}
		}
		return null;
	}
}
