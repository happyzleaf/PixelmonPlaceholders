package com.github.happyzleaf.pixelmonplaceholders.utility;

import com.pixelmonmod.pixelmon.api.world.WeatherType;
import com.pixelmonmod.pixelmon.database.DatabaseMoves;
import com.pixelmonmod.pixelmon.database.DatabaseStats;
import com.pixelmonmod.pixelmon.entities.npcs.registry.DropItemRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.registry.PokemonDropInformation;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.*;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.*;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import me.rojo8399.placeholderapi.NoValueException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/***************************************
 * PixelmonPlaceholders
 * Created on 07/06/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public class ParserUtility {
	public static Object parsePokedexInfo(EnumPokemon pokemon, String[] values) throws NoValueException {
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
							throw new NoValueException();
					}
					return rarity <= 0 ? EnumPokemon.legendaries.contains(pokemon.name) ? 0 : 1 : rarity;
				}
				break;
			case "postevolutions":
				return asReadableList(values, 2, Arrays.stream(stats.evolutions).map(evolution -> evolution.to.name).toArray());
			case "preevolutions":
				return asReadableList(values, 2, stats.preEvolutions);
			case "evolutions":
				return asReadableList(values, 2, ArrayUtils.addAll(ArrayUtils.add(Arrays.stream(stats.evolutions).map(evolution -> evolution.to.name).toArray(), pokemon.name), stats.preEvolutions));
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
			case "doesevolve": //1.2.2
				return stats.evolutions.length != 0;
			case "evolutionscount": //1.2.2
				return stats.evolutions.length;
			case "evolution": //Modified in 1.2.2
				if (values.length >= 3) {
					int evolution = Integer.parseInt(values[2]) - 1;
					if (stats.evolutions.length <= evolution) {
						return "Does not evolve.";
					} else {
						Evolution evol = stats.evolutions[evolution];
						if (values.length < 4) {
							return stats.evolutions[evolution].to.name;
						} else { //Drastically changed since 1.3.0
							EvoParser parser = evoParsers.get(values[4]);
							EvoCondition cond = null;
							for (EvoCondition c : evol.conditions) {
								if (c.getClass().equals(parser.clazz)) {
									cond = c;
								}
							}
							if (cond == null) throw new NoValueException();
							//noinspection unchecked
							return parser.parse(cond, values, 5);
						}
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
								}
							}
							break;
						case "yields":
							EVsStore yield = stats.evGain;
							return yield.HP + yield.Attack + yield.Defence + yield.SpecialAttack + yield.SpecialDefence + yield.Speed;
					}
				}
				break;
			case "drops":
				if (values.length >= 3) {
					HashMap<EnumPokemon, PokemonDropInformation> pokemonDrops = ReflectionHelper.getPrivateValue(DropItemRegistry.class, null, "pokemonDrops");
					PokemonDropInformation drops = pokemonDrops.get(pokemon);
					if (drops == null) {
						return "None";
					} else {
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
				}
				break;
			case "egggroups":
				return asReadableList(values, 2, stats.eggGroups);
			case "texturelocation": //Since 1.2.3
				return "pixelmon:sprites/pokemon/" + String.format("%03d", stats.nationalPokedexNumber);
		}
		throw new NoValueException();
	}
	
	public static Object parsePokemonInfo(EntityPixelmon pokemon, String[] values) throws NoValueException {
		if (values.length >= 2) {
			switch (values[1]) {
				case "nickname":
					return pokemon.hasNickname() ? pokemon.getNickname() : pokemon.getName();
				case "exp":
					return formatBigNumbers(pokemon.getLvl().getExp());
				case "level":
					return pokemon.getLvl().getLevel();
				case "exptolevelup":
					return formatBigNumbers(pokemon.getLvl().getExpForNextLevelClient());
				case "stats":
					if (values.length >= 3) {
						Stats stats = pokemon.stats;
						switch (values[2]) {
							case "hp":
								return stats.HP;
							case "atk":
								return stats.Attack;
							case "def":
								return stats.Defence;
							case "spa":
								return stats.SpecialAttack;
							case "spd":
								return stats.SpecialDefence;
							case "spe":
								return stats.Speed;
							case "ivs":
								if (values.length >= 4) {
									IVStore ivs = stats.IVs;
									switch (values[3]) {
										case "hp":
											return ivs.HP;
										case "atk":
											return ivs.Attack;
										case "def":
											return ivs.Defence;
										case "spa":
											return ivs.SpAtt;
										case "spd":
											return ivs.SpDef;
										case "spe":
											return ivs.Speed;
										case "total": //since 1.2.3
											return ivs.HP + ivs.Attack + ivs.Defence + ivs.SpAtt + ivs.SpDef + ivs.Speed;
										case "totalpercentage":
											String result3 = "" + (ivs.HP + ivs.Attack + ivs.Defence + ivs.SpAtt + ivs.SpDef + ivs.Speed) * 100 / 186;
											if (result3.substring(result3.indexOf(".") + 1).length() == 1) {
												return result3.substring(0, result3.length() - 2);
											} else {
												return result3.substring(0, result3.indexOf(".") + 3);
											}
									}
								}
								break;
							case "evs":
								if (values.length >= 4) {
									EVsStore evs = stats.EVs;
									switch (values[3]) {
										case "hp":
											return evs.HP;
										case "atk":
											return evs.Attack;
										case "def":
											return evs.Defence;
										case "spa":
											return evs.SpecialAttack;
										case "spd":
											return evs.SpecialDefence;
										case "spe":
											return evs.Speed;
										case "total": //since 1.2.3
											return evs.HP + evs.Attack + evs.Defence + evs.SpecialAttack + evs.SpecialDefence + evs.Speed;
										case "totalpercentage":
											String result4 = "" + (evs.HP + evs.Attack + evs.Defence + evs.SpecialAttack + evs.SpecialDefence + evs.Speed) / 510;
											if (result4.substring(result4.indexOf(".") + 1).length() == 1) {
												return result4.substring(0, result4.length() - 2);
											} else {
												return result4.substring(0, result4.indexOf(".") + 3);
											}
									}
								}
								break;
						}
					}
					break;
				case "helditem":
					return pokemon.heldItem == null ? "None" : pokemon.heldItem.getDisplayName();
				case "pos":
					if (values.length >= 3) {
						BlockPos pos = pokemon.getPosition();
						switch (values[2]) {
							case "x":
								return pos.getX();
							case "y":
								return pos.getY();
							case "z":
								return pos.getZ();
						}
					}
					break;
				case "moveset":
					Moveset moveset = pokemon.getMoveset();
					try {
						return moveset.get(Integer.parseInt(values[2]) - 1);
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
						return asReadableList(values, 2, moveset.attacks);
					}
				case "friendship":
					return formatBigNumbers(pokemon.friendship.getFriendship());
				case "ability":
					if (values.length == 2) { //why did i put this check here? UPDATE: Ohhh yeah so it is reflected over the generic placeholders
						return pokemon.getAbility().getName();
					}
					break;
				case "ball":
					return pokemon.caughtBall.name();
				//Since 1.2.0
				//case "possibledrops":
				//	return asReadableList(pokeValues, 2, DropItemRegistry.getDropsForPokemon(pokemon).stream().map(ParserUtility::getItemStackInfo).toArray());
				case "nature":
					return pokemon.getNature();
				case "gender": //since 1.2.3
					return pokemon.getGender().name();
				case "growth":
					return pokemon.getGrowth().name();
				case "shiny": //Since 1.3.0
					return pokemon.getIsShiny();
			}
		}
		
		return parsePokedexInfo(EnumPokemon.getFromNameAnyCase(pokemon.getPokemonName()), values);
	}
	
	/**
	 * @param values
	 * @param index The index in the array values where the method should start
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
		return is == null || is.getCount() == 0 ? "None" : is.getCount() + " " + is.getDisplayName();
	}
	
	/*
		TODO normalize function
		takes a string and returns the human-readable version of it
		MossyRock => Mossy Rock
		CLEAR => Clear
	 */
	
	//Please stop yelling at me, Sandy, not in front of our children!
	private static Map<String, EvoParser> evoParsers = new HashMap<>();
	static {
		evoParsers.put("biome", new EvoParser<BiomeCondition>(BiomeCondition.class) {
			@Override
			public Object parse(BiomeCondition condition, String[] values, int index) {
				return asReadableList(values, index, condition.biomes.stream().map(Biome::getBiomeName).toArray());
			}
		});
		evoParsers.put("chance", new EvoParser<ChanceCondition>(ChanceCondition.class) {
			@Override
			public Object parse(ChanceCondition condition, String[] values, int index) {
				return condition.chance;
			}
		});
		evoParsers.put("stone", new EvoParser<EvoRockCondition>(EvoRockCondition.class) {
			@Override
			public Object parse(EvoRockCondition condition, String[] values, int index) throws NoValueException {
				if (values[index + 1].equals("biome")) {
					if (values.length > index + 1) {
						return asReadableList(values, index + 1, Arrays.stream(condition.evoRock.biomes).map(Biome::getBiomeName).toArray());
					} else {
						throw new NoValueException();
					}
				}
				return condition.evoRock;
			}
		});
		evoParsers.put("friendship", new EvoParser<FriendshipCondition>(FriendshipCondition.class) {
			@Override
			public Object parse(FriendshipCondition condition, String[] values, int index) {
				return ReflectionHelper.getPrivateValue(FriendshipCondition.class, condition, "friendship");
			}
		});
		evoParsers.put("gender", new EvoParser<GenderCondition>(GenderCondition.class) {
			@Override
			public Object parse(GenderCondition condition, String[] values, int index) {
				return asReadableList(values, index, condition.genders.toArray());
			}
		});
		evoParsers.put("helditem", new EvoParser<HeldItemCondition>(HeldItemCondition.class) {
			@Override
			public Object parse(HeldItemCondition condition, String[] values, int index) {
				return condition.item.getLocalizedName();
			}
		});
		evoParsers.put("altitude", new EvoParser<HighAltitudeCondition>(HighAltitudeCondition.class) {
			@Override
			public Object parse(HighAltitudeCondition condition, String[] values, int index) {
				return condition.minAltitude;
			}
		});
		evoParsers.put("level", new EvoParser<LevelCondition>(LevelCondition.class) {
			@Override
			public Object parse(LevelCondition condition, String[] values, int index) {
				return ReflectionHelper.getPrivateValue(LevelCondition.class, condition, "level");
			}
		});
		evoParsers.put("move", new EvoParser<MoveCondition>(MoveCondition.class) {
			@Override
			public Object parse(MoveCondition condition, String[] values, int index) {
				return DatabaseMoves.getAttack(condition.attackIndex);
			}
		});
		evoParsers.put("movetype", new EvoParser<MoveTypeCondition>(MoveTypeCondition.class) {
			@Override
			public Object parse(MoveTypeCondition condition, String[] values, int index) {
				return ReflectionHelper.getPrivateValue(MoveTypeCondition.class, condition, "type");
			}
		});
		evoParsers.put("party", new EvoParser<PartyCondition>(PartyCondition.class) {
			@Override
			public Object parse(PartyCondition condition, String[] values, int index) throws NoValueException {
				if (values.length > index + 1) {
					if (values[index + 1].equals("pokemon")) {
						return asReadableList(values, index + 2, condition.withPokemon.toArray());
					} else if (values[index + 1].equals("type")) {
						return asReadableList(values, index + 2, condition.withTypes.toArray());
					}
				}
				throw new NoValueException();
			}
		});
		//TODO StatRatioCondition
		evoParsers.put("time", new EvoParser<TimeCondition>(TimeCondition.class) {
			@Override
			public Object parse(TimeCondition condition, String[] values, int index) {
				return condition.day ? "Day" : "Night";
			}
		});
		evoParsers.put("weather", new EvoParser<WeatherCondition>(WeatherCondition.class) {
			@Override
			public Object parse(WeatherCondition condition, String[] values, int index) {
				String w = ((WeatherType) ReflectionHelper.getPrivateValue(WeatherCondition.class, condition, "weather")).name();
				return w.substring(1) + w.substring(1, w.length() - 1);
			}
		});
	}
	
	public static abstract class EvoParser<T extends EvoCondition> {
		public Class<T> clazz;
		
		public EvoParser(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		public abstract Object parse(T condition, String[] values, int index) throws NoValueException;
	}
}
