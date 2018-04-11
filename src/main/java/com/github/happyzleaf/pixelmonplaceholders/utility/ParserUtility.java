package com.github.happyzleaf.pixelmonplaceholders.utility;

import com.github.happyzleaf.pixelmonplaceholders.PPConfig;
import com.pixelmonmod.pixelmon.api.world.WeatherType;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.database.DatabaseHelper;
import com.pixelmonmod.pixelmon.database.DatabaseMoves;
import com.pixelmonmod.pixelmon.database.DatabaseStats;
import com.pixelmonmod.pixelmon.entities.npcs.registry.DropItemRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.registry.PokemonDropInformation;
import com.pixelmonmod.pixelmon.entities.pixelmon.Entity6CanBattle;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.*;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.*;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.enums.EnumType;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import me.rojo8399.placeholderapi.NoValueException;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.entity.living.player.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.*;

/***************************************
 * PixelmonPlaceholders
 * Created on 07/06/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public class ParserUtility {
	private static HashMap<EnumPokemon, PokemonDropInformation> pokemonDrops;
	private static Field mainDrop, rareDrop, optDrop1, optDrop2;
	private static Field friendship, level, type, weather;
	
	static {
		try {
			Field pokemonDropsField = DropItemRegistry.class.getDeclaredField("pokemonDrops");
			pokemonDropsField.setAccessible(true);
			pokemonDrops = (HashMap<EnumPokemon, PokemonDropInformation>) pokemonDropsField.get(null);
			mainDrop = PokemonDropInformation.class.getDeclaredField("mainDrop");
			mainDrop.setAccessible(true);
			rareDrop = PokemonDropInformation.class.getDeclaredField("rareDrop");
			rareDrop.setAccessible(true);
			optDrop1 = PokemonDropInformation.class.getDeclaredField("optDrop1");
			optDrop1.setAccessible(true);
			optDrop2 = PokemonDropInformation.class.getDeclaredField("optDrop2");
			optDrop2.setAccessible(true);
			
			friendship = FriendshipCondition.class.getDeclaredField("friendship");
			friendship.setAccessible(true);
			level = LevelCondition.class.getDeclaredField("level");
			level.setAccessible(true);
			type = MoveTypeCondition.class.getDeclaredField("type");
			type.setAccessible(true);
			weather = WeatherCondition.class.getDeclaredField("weather");
			weather.setAccessible(true);
			
		} catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
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
				/*
				 * "postevolutions" does not work how you might think.
				 * The post evolution of Bulbasaur is only Ivysaur, while the post evolutions of Eevee are all the eevolutions.
				 * The problem is that even if i'd like to add Venusaur to the post evolutions of Bulbasaur, after Ivysaur,
				 * i can't know it programmatically without doing any kind of human sacrifice,
				 * and i wound't know if i'd have to add them (the evolutions of an evolution) only if the post evolutions is one or so,
				 * cause, even if i'm sure that Umbreon does not have a post evolution,
				 * i'm putting a big limit to the OOP which should work regardless of the poke.
				 *
				 * Yeah, i'm sure no one will understand what i've wrote, but don't worry, i'm the chosen, i'm gonna fix it myself and you won't even notice.
				 */
				return asReadableList(values, 2, Arrays.stream(stats.evolutions).map(evolution -> evolution.to.name).toArray());
			case "preevolutions":
				return asReadableList(values, 2, stats.preEvolutions);
			case "evolutions": //Evolutions in order since 1.3.0
				//WHAT AM I DOING
				return asReadableList(values, 2, ArrayUtils.addAll(ArrayUtils.add(ArrayUtils.addAll(new Object[]{}, stats.preEvolutions), pokemon.name), Arrays.stream(stats.evolutions).map(evolution -> evolution.to.name).toArray()));
			case "ability":
				if (values.length == 3) {
					String value1 = values[2];
					int ability = value1.equals("1") ? 0 : value1.equals("2") ? 1 : value1.equalsIgnoreCase("h") ? 2 : -1;
					if (ability != -1) {
						String result = stats.abilities[ability];
						return result == null ? PPConfig.noneText : result;
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
					int evolution;
					try {
						evolution = Integer.parseInt(values[2]) - 1;
					} catch (NumberFormatException e) {
						throw new NoValueException();
					}
					if (stats.evolutions.length <= 0) {
						throw new NoValueException();
					}
					if (stats.evolutions.length <= evolution) {
						return "Does not evolve.";
					} else {
						Evolution evol = stats.evolutions[evolution];
						if (values.length < 4) {
							return stats.evolutions[evolution].to.name;
						} else { //Drastically changed since 1.3.0
							String choice = values[3];
							if (choice.equals("list")) { //TODO write better
								List<String> conditions = new ArrayList<>();
								for (Map.Entry<String, EvoParser> entry : evoParsers.entrySet()) {
									for (EvoCondition cond : evol.conditions) {
										if (cond.getClass().equals(entry.getValue().clazz)) {
											conditions.add(entry.getKey());
										}
									}
								}
								if (!conditions.contains("level") && evol instanceof LevelingEvolution) {
									conditions.add("level");
								}
								return asReadableList(values, 4, conditions.toArray());
							} else {
								try {
									EvoParser parser = evoParsers.get(values[3]);
									if (parser == null) throw new NoValueException();
									EvoCondition cond = null;
									for (EvoCondition c : evol.conditions) {
										if (c.getClass().equals(parser.clazz)) {
											cond = c;
										}
									}
									if (cond == null) {
										if (values[3].equals("level") && evol instanceof LevelingEvolution) {
											return ((LevelingEvolution) evol).level;
										}
										throw new NoValueException();
									}
									try {
										//noinspection unchecked
										return parser.parse(cond, values, 4);
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									}
								} catch (NoValueException e) {
									return PPConfig.evolutionNotAvailableText;
								}
							}
							
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
					PokemonDropInformation drops = pokemonDrops.get(pokemon);
					if (drops == null) {
						return PPConfig.noneText;
					} else {
						try {
							switch (values[2]) {
								case "main":
									return getItemStackInfo((ItemStack) mainDrop.get(drops));
								case "rare":
									return getItemStackInfo((ItemStack) rareDrop.get(drops));
								case "optional1":
									return getItemStackInfo((ItemStack) optDrop1.get(drops));
								case "optional2":
									return getItemStackInfo((ItemStack) optDrop2.get(drops));
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
				break;
			case "egggroups":
				return asReadableList(values, 2, stats.eggGroups);
			case "texturelocation": //Since 1.2.3
				return "pixelmon:sprites/pokemon/" + String.format("%03d", stats.nationalPokedexNumber);
			case "move": //Since 1.3.0 TODO test
				if (values.length >= 3) {
					try {
						Object[] attacks = getAllAttackNames(stats);
						int attack = Integer.parseInt(values[2]) - 1;
						if (attack >= 0 && attack < attacks.length) {
							return attacks[attack];
						} else {
							return PPConfig.moveNotAvailableText;
						}
					} catch (NumberFormatException ignored) {
					}
				}
				break;
			case "moves": //TODO test
				return asReadableList(values, 2, getAllAttackNames(stats));
		}
		throw new NoValueException();
	}
	
	public static Object[] getAllAttackNames(BaseStats stats) {
		List<Attack> attacks = DatabaseMoves.getAllAttacks(stats.id);
		attacks.addAll(DatabaseMoves.getAllTutorAttacks(stats.id));
		attacks.addAll(getAllEggAttacks(stats));
		return attacks.stream().map(attack -> attack.baseAttack.getLocalizedName()).toArray();
	}
	
	/**
	 * TODO TEST
	 * Slightly mod of another method, but doesn't need an alive entity to get its data.
	 * @see DatabaseMoves#getAllEggAttacks(Entity6CanBattle)
	 */
	public static ArrayList<Attack> getAllEggAttacks(BaseStats stats) {
		ArrayList attacks = new ArrayList();
		
		try {
			Connection conn = DatabaseHelper.getConnection();
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select MOVEID from PIXELMONEGGSKILLS where PIXELMONID='" + stats.baseFormID + "'");
			
			while(rs.next()) {
				int moveID = rs.getInt("MOVEID");
				Attack attack = DatabaseMoves.getAttack(moveID);
				if (attack != null) {
					attacks.add(attack);
				}
			}
			
			rs.close();
		} catch (Exception var7) {
			;
		}
		
		return attacks;
	}
	
	public static Object parsePokemonInfo(Player player, PlayerStorage storage, int[] id, String[] values) throws NoValueException {
		if (id.length == 2 && id[0] == -1 && id[1] == -1) {
			return PPConfig.teamMemberNotAvailableText;
		}
		
		boolean isSentOut = true; //The nbt "IsInBall" might be incorrect, so we save the real value here, before we load the pixelmon.
		
		EntityPixelmon pokemon = storage.getAlreadyExists(id, (World) player.getWorld()).orElse(null);
		if (pokemon == null) {
			pokemon = storage.sendOut(id, (World) player.getWorld());
			isSentOut = false;
		}
		
		if (pokemon == null) {
			return PPConfig.entityNotFoundText;
		}
		
		if (pokemon.isEgg && PPConfig.disableEggInfo) {
			return PPConfig.disabledEggText;
		}
		//everything till this since 1.3.1
		
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
					return pokemon.heldItem == null ? PPConfig.noneText : pokemon.heldItem.getDisplayName();
				case "pos": //Before 1.3.0: last known position. After: exact pokémon position or player's position if carried in a ball
					if (values.length >= 3) {
						//Vector3d pos = isSentOut ? new Vector3d(pokemon.getPosition().getX(), pokemon.getPosition().getY(), pokemon.getPosition().getZ()) : player.getPosition();
						switch (values[2]) {
							case "x":
								//return pos.getX(); <-- this is way more elegant but i'd like to avoid creating a new vector on every placeholder request
								return formatDouble(isSentOut ? pokemon.getPosition().getX() : player.getPosition().getX()); //NOTE: This method won't work with 7.0.0!
							case "y":
								return formatDouble(isSentOut ? pokemon.getPosition().getY() : player.getPosition().getY());
							case "z":
								return formatDouble(isSentOut ? pokemon.getPosition().getZ() : player.getPosition().getZ());
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
				case "ball": //Updated 1.3.0: uses localized names
					return pokemon.caughtBall.getItem().getLocalizedName();
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
	 * @param index  The index in the array values where the method should start
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
		return list.isEmpty() ? PPConfig.noneText : list;
	}
	
	public static String formatBigNumbers(int number) {
		if (number < 1000) {
			return String.valueOf(number);
		} else if (number < 1000000) {
			return String.valueOf((double) Math.round(number / 100) / 10) + "k";
		} else if (number < 1000000000) {
			return String.valueOf((double) Math.round(number / 100000) / 10) + "m";
		} else {
			return String.valueOf((double) Math.round(number / 100000000) / 10) + "b";
		}
	}
	
	private static DecimalFormat formatter = new DecimalFormat();
	
	static {
		formatter.setMaximumFractionDigits(PPConfig.maxFractionDigits);
		formatter.setMinimumFractionDigits(PPConfig.minFractionDigits);
	}
	
	public static String formatDouble(double number) {
		return formatter.format(number);
	}
	
	public static Object getItemStackInfo(@Nullable ItemStack is) {
		return is == null || is.getCount() == 0 ? PPConfig.noneText : is.getCount() + " " + is.getDisplayName();
	}
	
	/*
		TODO normalize function
		takes a string and returns the human-readable version of it
		MossyRock => Mossy Rock
		CLEAR => Clear
	 */
	
	//Please stop yelling at me Sandy! not in front of our children!
	private static Map<String, EvoParser> evoParsers = new HashMap<>();
	
	static {
		evoParsers.put("biome", new EvoParser<BiomeCondition>(BiomeCondition.class) {
			@Override
			public Object parse(BiomeCondition condition, String[] values, int index) {
				return asReadableList(values, index, condition.biomes.stream().map(biome -> biome.biomeName).toArray());
			}
		});
		evoParsers.put("chance", new EvoParser<ChanceCondition>(ChanceCondition.class) {
			@Override
			public Object parse(ChanceCondition condition, String[] values, int index) {
				return formatDouble(condition.chance);
			}
		});
		evoParsers.put("stone", new EvoParser<EvoRockCondition>(EvoRockCondition.class) {
			@Override
			public Object parse(EvoRockCondition condition, String[] values, int index) throws NoValueException {
				if (values.length > index) {
					if (values[index].equals("biome")) {
						return asReadableList(values, index + 1, Arrays.stream(condition.evoRock.biomes).map(biome -> biome.biomeName).toArray());
					} else {
						throw new NoValueException();
					}
				}
				return condition.evoRock;
			}
		});
		evoParsers.put("friendship", new EvoParser<FriendshipCondition>(FriendshipCondition.class) {
			@Override
			public Object parse(FriendshipCondition condition, String[] values, int index) throws IllegalAccessException {
				int f = friendship.getInt(condition);
				return f == -1 ? 220 : f;
			}
		});
		evoParsers.put("gender", new EvoParser<GenderCondition>(GenderCondition.class) {
			@Override
			public Object parse(GenderCondition condition, String[] values, int index) {
				return asReadableList(values, index, condition.genders/*.stream().filter(gender -> gender != Gender.None)*/.toArray());
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
				return formatDouble(condition.minAltitude);
			}
		});
		evoParsers.put("level", new EvoParser<LevelCondition>(LevelCondition.class) {
			@Override
			public Object parse(LevelCondition condition, String[] values, int index) throws IllegalAccessException {
				return level.getInt(condition);
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
			public Object parse(MoveTypeCondition condition, String[] values, int index) throws IllegalAccessException {
				return ((EnumType) type.get(condition)).getLocalizedName();
			}
		});
		evoParsers.put("party", new EvoParser<PartyCondition>(PartyCondition.class) {
			@Override
			public Object parse(PartyCondition condition, String[] values, int index) throws NoValueException {
				if (values.length > index) {
					if (values[index].equals("pokemon")) {
						return asReadableList(values, index + 1, condition.withPokemon.toArray());
					} else if (values[index].equals("type")) {
						return asReadableList(values, index + 1, condition.withTypes.toArray());
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
			public Object parse(WeatherCondition condition, String[] values, int index) throws IllegalAccessException {
				String w = ((WeatherType) weather.get(condition)).name();
				return w.substring(1) + w.substring(1, w.length() - 1);
			}
		});
	}
	
	public static abstract class EvoParser<T extends EvoCondition> {
		public Class<T> clazz;
		
		public EvoParser(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		public abstract Object parse(T condition, String[] values, int index) throws NoValueException, IllegalAccessException;
	}
}
