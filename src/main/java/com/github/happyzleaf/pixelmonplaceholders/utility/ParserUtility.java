package com.github.happyzleaf.pixelmonplaceholders.utility;

import com.pixelmonmod.pixelmon.database.DatabaseMoves;
import com.pixelmonmod.pixelmon.database.DatabaseStats;
import com.pixelmonmod.pixelmon.entities.npcs.registry.DropItemRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.registry.PokemonDropInformation;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Stats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.*;
import com.pixelmonmod.pixelmon.enums.EnumEvolutionRock;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.items.ItemHeld;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;

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
				return asReadableList(values, 2, Arrays.stream(stats.evolutions).map(evolution -> evolution.evolveInto.name).toArray());
			case "preevolutions":
				return asReadableList(values, 2, stats.preEvolutions);
			case "evolutions":
				return asReadableList(values, 2, ArrayUtils.addAll(ArrayUtils.add(Arrays.stream(stats.evolutions).map(evolution -> evolution.evolveInto.name).toArray(), pokemon.name), stats.preEvolutions));
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
			//Since 1.2.0
			case "biomes":
				return asReadableList(values, 2, Arrays.stream(stats.biomeIDs).map(id -> Biome.getBiome(id).getBiomeName()).toArray());
			case "spawnlocations":
				return asReadableList(values, 2, stats.spawnLocations);
			case "evolutiontype":
				if (stats.evolutions.length > 0) {
					return stats.evolutions[0].data.getType();
				}
				break;
			case "evolution":
				if (values.length >= 3 && stats.evolutions.length > 0) {
					IEvolutionData evol = stats.evolutions[0].data;
					switch (values[2]) {
						case "bystone":
							if (evol instanceof EvolutionByStone) {
								if (values.length == 4) {
									EvolutionByStone stone = (EvolutionByStone) evol;
									switch (values[3]) {
										case "stone":
											return ReflectionHelper.getPrivateValue(EvolutionByStone.class, stone, "stone");
										case "specificgender":
											return ReflectionHelper.getPrivateValue(EvolutionByStone.class, stone, "evolveByGender");
										case "gender":
											return ReflectionHelper.getPrivateValue(EvolutionByStone.class, stone, "male") ? "Male" : "Female";
									}
								}
							} else {
								return "Cannot evolve by stone.";
							}
						case "friendship":
							if (evol instanceof EvolutionFriendship) {
								if (values.length == 4) {
									EvolutionFriendship friendship = (EvolutionFriendship) evol;
									switch (values[3]) {
										case "specifictime":
											return ReflectionHelper.getPrivateValue(EvolutionFriendship.class, friendship, "hasCondition");
										case "time":
											return ReflectionHelper.getPrivateValue(EvolutionFriendship.class, friendship, "condition");
									}
								}
							} else {
								return "Cannot evolve by friendship.";
							}
							break;
						case "level":
							if (evol instanceof EvolutionLevel) {
								if (values.length == 4) {
									EvolutionLevel level = (EvolutionLevel) evol;
									switch (values[3]) {
										case "specificlevel":
											return ReflectionHelper.getPrivateValue(EvolutionLevel.class, level, "hasEvolveLevel");
										case "level":
											return ReflectionHelper.getPrivateValue(EvolutionLevel.class, level, "evolveLevel");
										case "specificbiomes":
											return ReflectionHelper.getPrivateValue(EvolutionLevel.class, level, "biomeCondition");
										case "biome":
											return ReflectionHelper.getPrivateValue(EvolutionLevel.class, level, "biome");
										case "specificgender":
											return ReflectionHelper.getPrivateValue(EvolutionLevel.class, level, "hasGenderCondition");
										case "gender":
											return ReflectionHelper.getPrivateValue(EvolutionLevel.class, level, "gender");
									}
								}
							} else {
								return "Cannot evolve by level.";
							}
							break;
						case "move":
							if (evol instanceof EvolutionMove) {
								return DatabaseMoves.getAttack(ReflectionHelper.getPrivateValue(EvolutionMove.class, (EvolutionMove) evol, "moveIndex"));
							} else {
								return "Cannot evolve by move.";
							}
						case "proximity":
							if (evol instanceof EvolutionProximity) {
								if (values.length == 4) {
									EvolutionProximity proximity = (EvolutionProximity) evol;
									EnumEvolutionRock rock = ReflectionHelper.getPrivateValue(EvolutionProximity.class, proximity, "rock");
									switch (values[3]) {
										case "rock":
											return rock;
										case "biomes":
											asReadableList(values, 4, Arrays.stream(rock.biomes).map(Biome::getBiomeName).toArray());
									}
								}
							} else {
								return "Cannot evolve by proximity to rock.";
							}
							break;
						case "trade":
							if (evol instanceof EvolutionTrade) {
								if (values.length == 4) {
									EvolutionTrade trade = (EvolutionTrade) evol;
									switch(values[3]) {
										case "specificitem":
											return ReflectionHelper.getPrivateValue(EvolutionTrade.class, trade, "hasItemCondition");
										case "item":
											return ((ItemHeld) ReflectionHelper.getPrivateValue(EvolutionTrade.class, trade, "item")).getLocalizedName();
									}
								}
							} else {
								return "Cannot evolve by trade.";
							}
							break;
					}
				}
				break;
			case "type":
				return asReadableList(values, 2, stats.getTypeList().toArray());
			case "basestats":
				if (values.length >= 3) {
					switch (values[2]) {
						case "hp":
							return stats.hp;
						case "atk":
							return stats.attack;
						case "def":
							return stats.defence;
						case "spa":
							return stats.spAtt;
						case "spd":
							return stats.spDef;
						case "spe":
							return stats.speed;
						case "yield":
							if (values.length >= 4) {
								EVsStore yield = stats.evGain;
								switch (values[3]) {
									case "hp":
										return yield.HP;
									case "atk":
										return yield.Attack;
									case "def":
										return yield.Defence;
									case "spa":
										return yield.SpecialAttack;
									case "spd":
										return yield.SpecialDefence;
									case "spe":
										return yield.Speed;
									case "total":
										return yield.HP + yield.Attack + yield.Defence + yield.SpecialAttack + yield.SpecialDefence + yield.Speed;
								}
							}
							break;
					}
				}
				break;
			case "drops":
				if (values.length >= 3) {
					HashMap<EnumPokemon, PokemonDropInformation> pokemonDrops = ReflectionHelper.getPrivateValue(DropItemRegistry.class, null, "pokemonDrops");
					PokemonDropInformation drops = pokemonDrops.get(pokemon);
					switch (values[2]) {
						case "main":
							return getItemStackInfo(ReflectionHelper.getPrivateValue(PokemonDropInformation.class, drops, "mainDrop"));
						case "rare":
							return getItemStackInfo(ReflectionHelper.getPrivateValue(PokemonDropInformation.class, drops, "rareDrop"));
						case "optional1":
							return getItemStackInfo(ReflectionHelper.getPrivateValue(PokemonDropInformation.class, drops, "optDrop1"));
						case "optional2":
							return getItemStackInfo(ReflectionHelper.getPrivateValue(PokemonDropInformation.class, drops, "optDrop2"));
					}
				}
				break;
			case "egggroup":
				return asReadableList(values, 2, stats.eggGroups);
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
	
	public static String getItemStackInfo(@Nullable ItemStack is) {
		return is == null || is.stackSize == 0 ? "None" : is.stackSize + " " + is.getDisplayName();
	}
}
