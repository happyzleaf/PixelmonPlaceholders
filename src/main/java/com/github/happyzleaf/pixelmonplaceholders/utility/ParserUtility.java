package com.github.happyzleaf.pixelmonplaceholders.utility;

import com.pixelmonmod.pixelmon.database.DatabaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import org.apache.commons.lang3.ArrayUtils;

/***************************************
 * PixelmonPlaceholders
 * Created on 07/06/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public class ParserUtility {
	public static Object parsePokedexInfo(EnumPokemon pokemon, String[] values) {
		if (values.length == 1) {
			return pokemon.name;
		}
		
		BaseStats stats = DatabaseStats.getBaseStats(pokemon.name).orElse(null);
		if (stats == null) {
			throw new RuntimeException("Could not find BaseStats for pokémon " + pokemon.name + ".");
		}
		
		switch (values[1]) {
			case "name":
				return pokemon.name;
			case "catchrate":
				return stats.catchRate;
			case "nationalid":
				return stats.nationalPokedexNumber;
			case "rarity":
				if (values.length == 3) {
					int rarity;
					switch (values[2]) {
						case "day":
							rarity = stats.rarity.day;
							break;
						case "night":
							rarity = stats.rarity.night;
							break;
						case "dawndusk":
							rarity = stats.rarity.dawndusk;
							break;
						default:
							return null;
					}
					return rarity <= 0 ? EnumPokemon.legendaries.contains(pokemon.name) ? 0 : 1 : rarity;
				}
				break;
			case "postevolutions":
				return asReadableList(values, 2, stats.evolutions);
			case "preevolutions":
				return asReadableList(values, 2, stats.preEvolutions);
			case "evolutions":
				return asReadableList(values, 2, ArrayUtils.addAll(ArrayUtils.add(stats.evolutions, pokemon.name), stats.preEvolutions));
			case "ability":
				if (values.length == 3) {
					String value1 = values[2];
					int ability = value1.equals("1") ? 0 : value1.equals("2") ? 1 : value1.equalsIgnoreCase("h") ? 2 : -1;
					if (ability != -1) {
						String result = stats.abilities[ability];
						return result == null ? "None" : result;
					}
				}
				break;
			case "abilities":
				return asReadableList(values, 2, stats.abilities);
		}
		return null;
	}
	
	/**
	 * @param values
	 * @param index  the index in the array values where the method should start
	 * @param data
	 * @return
	 */
	public static Object asReadableList(String[] values, int index, Object[] data) {
		String separator = ", ";
		if (values.length == index + 1) {
			separator = values[index].replaceAll("--", " ");
		}
		String list = "";
		for (Object s : data) {
			if (s != null) {
				if (s.equals(data[0])) {
					list = s.toString();
				} else {
					list = list.concat(separator + s);
				}
			}
		}
		return list.isEmpty() ? "None" : list;
	}
	
	public static Object formatBigNumbers(int number) {
		if (number < 1000) {
			return String.valueOf(number);
		} else if (number < 1000000) {
			return String.valueOf((double) Math.round(number/100)/10) + "k";
		} else if (number < 1000000000) {
			return String.valueOf((double) Math.round(number/100000)/10) + "m";
		} else {
			return String.valueOf((double) Math.round(number/100000000)/10) + "b";
		}
	}
}
