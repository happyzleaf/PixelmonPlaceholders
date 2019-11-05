package com.github.happyzleaf.pixelmonplaceholders.utility;

import com.github.happyzleaf.pixelmonplaceholders.PPConfig;
import com.pixelmonmod.pixelmon.api.pokemon.ISpecType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.pokemon.SpecFlag;
import com.pixelmonmod.pixelmon.api.world.WeatherType;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.AttackBase;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.client.gui.GuiResources;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.SpawnLocationType;
import com.pixelmonmod.pixelmon.entities.npcs.registry.DropItemRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.registry.PokemonDropInformation;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.*;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MeltanStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.EnumType;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.items.heldItems.HeldItem;
import me.rojo8399.placeholderapi.NoValueException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.entity.Entity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ParserUtility {
	private static Field mainDrop_f, rareDrop_f, optDrop1_f, optDrop2_f;
	private static Field friendship_f, level_f, type_f, weather_f;
	private static Field biomeName_f;
	
	static {
		try {
			mainDrop_f = PokemonDropInformation.class.getDeclaredField("mainDrop");
			mainDrop_f.setAccessible(true);
			rareDrop_f = PokemonDropInformation.class.getDeclaredField("rareDrop");
			rareDrop_f.setAccessible(true);
			optDrop1_f = PokemonDropInformation.class.getDeclaredField("optDrop1");
			optDrop1_f.setAccessible(true);
			optDrop2_f = PokemonDropInformation.class.getDeclaredField("optDrop2");
			optDrop2_f.setAccessible(true);
			
			friendship_f = FriendshipCondition.class.getDeclaredField("friendship");
			friendship_f.setAccessible(true);
			level_f = LevelCondition.class.getDeclaredField("level");
			level_f.setAccessible(true);
			type_f = MoveTypeCondition.class.getDeclaredField("type");
			type_f.setAccessible(true);
			weather_f = WeatherCondition.class.getDeclaredField("weather");
			weather_f.setAccessible(true);
			
			biomeName_f = Arrays.stream(Biome.class.getDeclaredFields()).filter(field -> field.getName().equals("field_76791_y") || field.getName().equals("biomeName")).findFirst().get(); // Sorry I'm lazy and I hate this plugin anyway.
			biomeName_f.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param from inclusive
	 * @param to   exclusive
	 */
	public static <T> T[] copyOfRange(T[] original, int from, int to) {
		if (original.length == to - from) {
			try {
				return (T[]) original.getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return Arrays.copyOfRange(original, from, to);
		}
	}
	
	public static Object parsePokedexInfo(EnumSpecies pokemon, @Nullable IEnumForm form, String[] values) throws NoValueException {
		if (values.length == 0) {
			return pokemon.getLocalizedName();
		}
		
		BaseStats stats = form == null ? pokemon.getBaseStats() : pokemon.getBaseStats(form);
		
		switch (values[0]) {
			case "name":
				return pokemon.getLocalizedName();
			case "catchrate":
				return stats.catchRate;
			case "nationalid":
				return stats.nationalPokedexNumber;
			case "rarity": // TODO add
				throw new NoValueException("rarity has been disabled for now");
				/*if (values.length == 2) {
//					int rarity;
					switch (values[1]) {
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
					// return rarity <= 0 ? EnumPokemon.legendaries.contains(pokemon.name) ? 0 : 1 : rarity;
				}
				break;*/
			case "postevolutions":
				return asReadableList(values, 1, stats.evolutions.stream().map(evolution -> getPokemonName(evolution.to.name)).toArray());
			case "preevolutions":
				return asReadableList(values, 1, Arrays.stream(stats.preEvolutions).map(EnumSpecies::getPokemonName).toArray());
			case "evolutions": {
				List<String> evolutions = new ArrayList<>(Arrays.stream(stats.preEvolutions).map(EnumSpecies::getPokemonName).collect(Collectors.toList()));
				evolutions.add(pokemon.getLocalizedName());
				evolutions.addAll(stats.evolutions.stream().map(evolution -> getPokemonName(evolution.to.name)).collect(Collectors.toList()));
				return asReadableList(values, 1, evolutions.toArray());
			}
			case "ability":
				if (values.length > 1) {
					String value1 = values[1];
					int ability = value1.equals("1") ? 0 : value1.equals("2") ? 1 : value1.toLowerCase().equals("h") ? 2 : -1;
					if (ability != -1) {
						String result = stats.abilities[ability];
						return result == null ? PPConfig.noneText : result;
					}
					throwWrongInput("1", "2", "h");
				} else {
					throw new NoValueException("Not enough arguments.");
				}
			case "abilities":
				return asReadableList(values, 1, Arrays.stream(stats.abilities).map(s -> I18n.translateToLocal("abiility." + s + ".name")).toArray());
			case "biomes": // TODO add
				throw new NoValueException("biomes have been disabled for now");
				//return asReadableList(values, 2, Arrays.stream(stats.biomeIDs).map(id -> Biome.getBiome(id).getBiomeName()).toArray());
			case "spawnlocations":
				return asReadableList(values, 1, Arrays.stream(stats.spawnLocations).map(SpawnLocationType::getLocalizedName).toArray());
			case "doesevolve":
				return stats.evolutions.size() != 0;
			case "evolutionscount":
				return stats.evolutions.size();
			case "evolution":
				if (values.length > 1) {
					int evolution;
					try {
						evolution = Integer.parseInt(values[1]) - 1;
					} catch (NumberFormatException e) {
						throw new NoValueException();
					}
					if (stats.evolutions.size() <= 0) {
						throw new NoValueException();
					}
					if (stats.evolutions.size() <= evolution) {
						return "Does not evolve.";
					} else {
						Evolution evol = stats.evolutions.get(evolution);
						if (values.length < 3) {
							return getPokemonName(stats.evolutions.get(evolution).to.name);
						} else {
							String choice = values[2];
							if (choice.equals("list")) { //TODO write better
								List<String> conditions = new ArrayList<>();
								for (Map.Entry<String, EvoParser> entry : evoParsers.entrySet()) {
									for (EvoCondition cond : evol.conditions) {
										if (cond.getClass().equals(entry.getValue().clazz)) {
											conditions.add(entry.getKey());
										}
									}
								}
								return asReadableList(values, 3, conditions.toArray());
							} else {
								try {
									EvoParser parser = evoParsers.get(values[2]);
									if (parser == null) throw new NoValueException();
									EvoCondition cond = null;
									for (EvoCondition c : evol.conditions) {
										if (c.getClass().equals(parser.clazz)) {
											cond = c;
											break;
										}
									}
									if (cond == null) {
										throw new NoValueException(String.format("The condition %s isn't valid.", values[2]));
									}
									try {
										//noinspection unchecked
										return parser.parse(cond, values, 3);
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
				return asReadableList(values, 1, stats.getTypeList().stream().map(EnumType::getLocalizedName).toArray());
			case "basestats":
				if (values.length > 1) {
					switch (values[1]) {
						case "hp":
							return stats.stats.get(StatsType.HP);
						case "atk":
							return stats.stats.get(StatsType.Attack);
						case "def":
							return stats.stats.get(StatsType.Defence);
						case "spa":
							return stats.stats.get(StatsType.SpecialAttack);
						case "spd":
							return stats.stats.get(StatsType.SpecialDefence);
						case "spe":
							return stats.stats.get(StatsType.Speed);
						case "yield":
							if (values.length > 2) {
								switch (values[2]) {
									case "hp":
										return stats.evYields.get(StatsType.HP);
									case "atk":
										return stats.evYields.get(StatsType.Attack);
									case "def":
										return stats.evYields.get(StatsType.Defence);
									case "spa":
										return stats.evYields.get(StatsType.SpecialAttack);
									case "spd":
										return stats.evYields.get(StatsType.SpecialDefence);
									case "spe":
										return stats.evYields.get(StatsType.Speed);
									default:
										throwWrongInput("hp", "atk", "def", "spa", "spd", "spe");
								}
							}
							break;
						case "yields":
							return stats.evYields.values().stream().mapToInt(value -> value).sum();
						default:
							throwWrongInput("hp", "atk", "def", "spa", "spd", "spe", "yield", "yields");
					}
				}
				break;
			case "drops":
				if (values.length > 1) {
					Set<PokemonDropInformation> drops = DropItemRegistry.pokemonDrops.get(pokemon);
					if (drops == null) {
						return PPConfig.noneText;
					} else {
						switch (values[1]) {
							case "main":
								return getDropsInfo(values, 2, drops, mainDrop_f);
							case "rare":
								return getDropsInfo(values, 2, drops, rareDrop_f);
							case "optional1":
								return getDropsInfo(values, 2, drops, optDrop1_f);
							case "optional2":
								return getDropsInfo(values, 2, drops, optDrop2_f);
							default:
								throwWrongInput("main", "rare", "optional1", "optional2");
						}
					}
				}
				break;
			case "egggroups":
				return asReadableList(values, 1, stats.eggGroups);
			case "texturelocation":
				return "pixelmon:sprites/" + GuiResources.getSpritePath(pokemon, stats.form, stats.malePercent > 0 ? Gender.Male : Gender.Female, false, false);
			case "move":
				if (values.length > 1) {
					try {
						Object[] attacks = getAllAttackNames(stats);
						int attack = Integer.parseInt(values[1]) - 1;
						if (attack >= 0 && attack < attacks.length) {
							return attacks[attack];
						} else {
							return PPConfig.moveNotAvailableText;
						}
					} catch (NumberFormatException ignored) {}
				}
				break;
			case "moves":
				return asReadableList(values, 1, getAllAttackNames(stats));
		}
		throw new NoValueException();
	}
	
	public static void throwWrongInput(Object... expectedValues) throws NoValueException {
		throw new NoValueException("Wrong input." + (expectedValues.length > 0 ? " Expected values: " + Arrays.toString(expectedValues) : ""));
	}
	
	public static boolean checkSpecies(String name, Pokemon pokemon, EnumSpecies... species) throws NoValueException {
		if (Arrays.stream(species).noneMatch(s -> s == pokemon.getSpecies())) {
			throw new NoValueException(String.format("Wrong input. '%s' can only be used by %s.", name, Arrays.toString(species)));
		}
		
		return true;
	}
	
	public static Object[] getAllAttackNames(BaseStats stats) {
		return stats.getAllMoves().stream().map(attack -> attack.getActualMove().getLocalizedName()).toArray();
	}
	
	public static Object parsePokemonInfo(Entity owner, Pokemon pokemon, String[] values) throws NoValueException {
		if (pokemon == null) {
			return PPConfig.teamMemberNotAvailableText;
		}
		
		if (PPConfig.disableEggInfo && pokemon.isEgg()) {
			if (values.length != 1 || !values[0].equals("texturelocation")) {
				return PPConfig.disabledEggText;
			}
		}
		
		if (values.length > 0) {
			switch (values[0]) {
				case "nickname":
					return pokemon.getDisplayName();
				case "exp":
					return formatBigNumbers(pokemon.getExperience());
				case "level":
					return pokemon.getLevel();
				case "exptolevelup":
					return formatBigNumbers(pokemon.getExperienceToLevelUp());
				case "stats":
					if (values.length > 1) {
						switch (values[1]) {
							case "hp":
								return pokemon.getStats().hp;
							case "atk":
								return pokemon.getStats().attack;
							case "def":
								return pokemon.getStats().defence;
							case "spa":
								return pokemon.getStats().specialAttack;
							case "spd":
								return pokemon.getStats().specialDefence;
							case "spe":
								return pokemon.getStats().speed;
							case "ivs":
								if (values.length > 2) {
									switch (values[2]) {
										case "hp":
											return pokemon.getStats().ivs.hp;
										case "atk":
											return pokemon.getStats().ivs.attack;
										case "def":
											return pokemon.getStats().ivs.defence;
										case "spa":
											return pokemon.getStats().ivs.specialAttack;
										case "spd":
											return pokemon.getStats().ivs.specialDefence;
										case "spe":
											return pokemon.getStats().ivs.speed;
										case "total":
											return Arrays.stream(pokemon.getStats().ivs.getArray()).sum();
										case "totalpercentage":
											return formatDouble((Arrays.stream(pokemon.getStats().ivs.getArray()).sum()) * 100 / 186d);
										default:
											throwWrongInput("hp", "atk", "def", "spa", "spd", "spe", "total", "totalpercentage");
									}
								}
								break;
							case "evs":
								if (values.length > 2) {
									switch (values[2]) {
										case "hp":
											return pokemon.getStats().evs.hp;
										case "atk":
											return pokemon.getStats().evs.attack;
										case "def":
											return pokemon.getStats().evs.defence;
										case "spa":
											return pokemon.getStats().evs.specialAttack;
										case "spd":
											return pokemon.getStats().evs.specialDefence;
										case "spe":
											return pokemon.getStats().evs.speed;
										case "total":
											return Arrays.stream(pokemon.getStats().evs.getArray()).sum();
										case "totalpercentage":
											return formatDouble(Arrays.stream(pokemon.getStats().evs.getArray()).sum() * 100 / 510d);
										default:
											throwWrongInput("hp", "atk", "def", "spa", "spd", "spe", "total", "totalpercentage");
									}
								}
								break;
							default:
								throwWrongInput("hp", "atk", "def", "spa", "spd", "spe", "ivs", "evs");
						}
					}
					break;
				case "helditem":
					return pokemon.getHeldItem() == ItemStack.EMPTY ? PPConfig.noneText : pokemon.getHeldItem().getDisplayName();
				case "pos":
					if (values.length > 1) {
						EntityPixelmon entity = pokemon.getPixelmonIfExists();
						BlockPos pos = entity == null ? ((net.minecraft.entity.Entity) owner).getPosition() : entity.getPosition();
						switch (values[1]) {
							case "x":
								return pos.getX();
							case "y":
								return pos.getY();
							case "z":
								return pos.getZ();
							default:
								throwWrongInput("x", "y", "z");
						}
					}
					break;
				case "moveset":
					Moveset moveset = pokemon.getMoveset();
					try {
						int moveIndex = Integer.parseInt(values[1]) - 1;
						if (moveIndex < 0 || moveIndex >= 4) {
							throw new NoValueException("The attack index must be between 0 and 4.");
						}
						return moveset.get(moveIndex);
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
						return asReadableList(values, 1, Arrays.stream(moveset.attacks).map(attack -> attack.getActualMove().getLocalizedName()).toArray());
					}
				case "friendship":
					return formatBigNumbers(pokemon.getFriendship());
				case "ability":
					if (values.length == 1) {
						return pokemon.getAbility().getLocalizedName();
					} else if (values[1].equals("slot")) {
						return pokemon.getAbilitySlot() == 2 ? "H" : pokemon.getAbilitySlot() + 1;
					}
					
					break;
				case "ball":
					return pokemon.getCaughtBall().getItem().getLocalizedName();
				//case "possibledrops":
				//	return asReadableList(pokeValues, 1, DropItemRegistry.getDropsForPokemon(pokemon).stream().map(ParserUtility::getItemStackInfo).toArray());
				case "nature": {
					EnumNature nature = pokemon.getNature();
					if (values.length > 1) {
						switch (values[1]) {
							case "increased":
								return nature.increasedStat == StatsType.None ? PPConfig.noneText : nature.increasedStat.getLocalizedName();
							case "decreased":
								return nature.decreasedStat == StatsType.None ? PPConfig.noneText : nature.decreasedStat.getLocalizedName();
						}
						
						throwWrongInput("increased", "decreased");
					}
					
					return nature;
				}
				case "gender":
					return pokemon.getGender().getLocalizedName();
				case "growth":
					return pokemon.getGrowth().getLocalizedName();
				case "shiny":
					return pokemon.isShiny();
				case "hiddenpower":
					return HiddenPower.getHiddenPowerType(pokemon.getStats().ivs);
				case "texturelocation": {
					if (pokemon.isEgg()) {
						EnumSpecies species = pokemon.getSpecies();
						int cycles = pokemon.getEggCycles();
						return "pixelmon:sprites/eggs/"
								+ (species == EnumSpecies.Togepi ? "togepi" : species == EnumSpecies.Manaphy ? "manaphy" : "egg")
								+ (cycles > 10 ? "1" : cycles > 5 ? "2" : "3");
					} else {
						return "pixelmon:" + GuiResources.getSpritePath(pokemon.getSpecies(), pokemon.getForm(), pokemon.getGender(), pokemon.getSpecialTexture() != EnumSpecialTexture.None, pokemon.isShiny());
					}
				}
				case "customtexture":
					String custom = pokemon.getCustomTexture();
					if (custom != null && !custom.isEmpty()) {
						return custom;
					}
					
					return PPConfig.noneText;
				
				case "form":
					return pokemon.getForm();
				case "extraspecs":
					if (values.length > 1) {
						ISpecType spec = PokemonSpec.getSpecForKey(values[1]);
						if (spec instanceof SpecFlag) { //Could move to SpecType<Boolean> but let's imply this is the standard for boolean-based specs.
							return ((SpecFlag) spec).matches(pokemon);
						}
						
						throw new NoValueException("Spec not supported.");
					}
					
					throw new NoValueException("Not enough arguments.");
				case "aura":
					return getAuraID(pokemon);
				case "originaltrainer":
					if (values.length > 1) {
						switch (values[1]) {
							case "name":
								return pokemon.getOriginalTrainer() == null ? PPConfig.noneText : pokemon.getOriginalTrainer();
							case "uuid":
								return pokemon.getOriginalTrainerUUID() == null ? PPConfig.noneText : pokemon.getOriginalTrainerUUID();
						}
					}
					
					throwWrongInput("name", "uuid");
				case "eggsteps": // since 2.1.4
					if (!pokemon.isEgg()) {
						throw new NoValueException("The pixelmon is not in an egg.");
					}
					
					if (values.length > 1) {
						int current = pokemon.getEggCycles() * PixelmonConfig.stepsPerEggCycle + pokemon.getEggSteps();
						switch (values[1]) {
							case "current":
								return current;
							case "needed":
								return pokemon.getBaseStats().eggCycles * PixelmonConfig.stepsPerEggCycle - current;
						}
					}
					
					throwWrongInput("current", "needed");
				case "mew":
					if (checkSpecies("mew", pokemon, EnumSpecies.Mew)) {
						if (values.length > 1 && values[1].equals("clones")) {
							MewStats mew = (MewStats) pokemon.getExtraStats();
							if (values.length > 2) {
								switch (values[2]) {
									case "used":
										return mew.numCloned == MewStats.MAX_CLONES;
									case "total":
										return mew.numCloned;
								}
							}
							
							return mew.numCloned + "/" + MewStats.MAX_CLONES;
						}
						
						throwWrongInput("clones");
					}
				case "laketrio":
					if (checkSpecies("laketrio", pokemon, EnumSpecies.Uxie, EnumSpecies.Mesprit, EnumSpecies.Azelf)) {
						if (values.length > 1 && values[1].equals("rubies")) {
							LakeTrioStats lakeTrio = (LakeTrioStats) pokemon.getExtraStats();
							if (values.length > 2) {
								switch (values[2]) {
									case "used":
										return lakeTrio.numEnchanted == PixelmonConfig.lakeTrioMaxEnchants;
									case "total":
										return lakeTrio.numEnchanted;
								}
							}
							
							return lakeTrio.numEnchanted + "/" + PixelmonConfig.lakeTrioMaxEnchants;
						}
						
						throwWrongInput("rubies");
					}
				case "meltan":
					if (checkSpecies("meltan", pokemon, EnumSpecies.Meltan)) {
						MeltanStats meltan = (MeltanStats) pokemon.getExtraStats();
						if (values.length > 1 && values[1].equals("oressmelted")) {
							if (values.length > 2) {
								switch (values[2]) {
									case "used":
										return meltan.oresSmelted > 0;
									case "total":
										return meltan.oresSmelted;
								}
							}
							
							throwWrongInput("used", "total");
						}
						
						throwWrongInput("oressmelted");
					}
			}
		}
		
		return parsePokedexInfo(pokemon.getSpecies(), pokemon.getFormEnum(), values);
	}
	
	@Nullable
	public static String getAuraID(Pokemon pokemon) {
		NBTTagCompound persistentData = pokemon.getPersistentData();
		if (persistentData.hasKey("entity-particles:particle")) {
			return persistentData.getString("entity-particles:particle");
		} else {
			return checkForSpongeData(persistentData);
		}
	}
	
	@Nullable
	private static String checkForSpongeData(NBTTagCompound data) {
		NBTTagList manipulators = data.getCompoundTag("SpongeData").getTagList("CustomManipulators", Constants.NBT.TAG_COMPOUND);
		NBTTagCompound manipulator = (NBTTagCompound) StreamSupport.stream(manipulators.spliterator(), false)
				.filter(nbt -> nbt instanceof NBTTagCompound && "entity-particles:particle".equals(((NBTTagCompound) nbt).getString("ManipulatorId")))
				.findAny().orElse(null);
		return manipulator == null ? null : manipulator.getCompoundTag("ManipulatorData").getString("Id");
	}
	
	/**
	 * @param index The index in the array values where the method should start
	 */
	public static Object asReadableList(String[] values, int index, Object[] data) {
		if (data == null) return "";
		String separator = ", ";
		if (values.length == index + 1) {
			separator = values[index].replaceAll("--", " ");
		}
		String list = "";
		for (int i = 0; i < data.length; i++) {
			Object d = data[i];
			if (d == null) continue;
			if (list.isEmpty()) {
				list = d.toString();
			} else {
				list = list.concat(separator + d.toString());
			}
		}
		return list.isEmpty() ? PPConfig.noneText : list;
	}
	
	public static String normalizeText(String text) {
		return text.substring(1) + text.substring(1, text.length() - 1);
	}
	
	public static String formatBigNumbers(int number) {
		if (number < 1000) {
			return String.valueOf(number);
		} else if (number < 1000000) {
			return (double) Math.round(number / 100) / 10 + "k";
		} else if (number < 1000000000) {
			return (double) Math.round(number / 100000) / 10 + "m";
		} else {
			return (double) Math.round(number / 100000000) / 10 + "b";
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
	
	public static Object getDropsInfo(String[] values, int index, Set<PokemonDropInformation> drops, Field field) {
		try {
			List<Object> results = new ArrayList<>();
			for (PokemonDropInformation drop : drops) {
				results.add(getItemStackInfo((ItemStack) field.get(drop)));
			}
			return asReadableList(values, index, results.toArray());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Object getItemStackInfo(@Nullable ItemStack is) {
		return is == null || is.getCount() == 0 ? PPConfig.noneText : is.getCount() + " " + is.getDisplayName();
	}
	
	public static String getPokemonName(String pokemon) {
		return I18n.translateToLocal("pixelmon." + pokemon.toLowerCase() + ".name");
	}
	
	private static Map<String, EvoParser> evoParsers = new HashMap<>();
	
	static {
		evoParsers.put("biome", new EvoParser<BiomeCondition>(BiomeCondition.class) {
			@Override
			public Object parse(BiomeCondition condition, String[] values, int index) { //TODO CACHE!
				return asReadableList(values, index, condition.biomes.stream().map(biome -> ForgeRegistries.BIOMES.getValues().stream().filter(b -> {
					try {
						return biome.equalsIgnoreCase((String) biomeName_f.get(b));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						return false;
					}
				}).findFirst().get()).toArray());
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
						return asReadableList(values, index + 1, Arrays.stream(condition.evolutionRock.biomes).map(biome -> {
							try {
								return biomeName_f.get(biome);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
								return null;
							}
						}).filter(Objects::nonNull).toArray());
					} else {
						throw new NoValueException();
					}
				}
				return condition.evolutionRock;
			}
		});
		evoParsers.put("friendship", new EvoParser<FriendshipCondition>(FriendshipCondition.class) {
			@Override
			public Object parse(FriendshipCondition condition, String[] values, int index) throws IllegalAccessException {
				int f = friendship_f.getInt(condition);
				return f == -1 ? 220 : f;
			}
		});
		evoParsers.put("gender", new EvoParser<GenderCondition>(GenderCondition.class) {
			@Override
			public Object parse(GenderCondition condition, String[] values, int index) {
				return asReadableList(values, index, condition.genders.stream().map(Gender::getLocalizedName).toArray());
			}
		});
		evoParsers.put("helditem", new EvoParser<HeldItemCondition>(HeldItemCondition.class) {
			@Override
			public Object parse(HeldItemCondition condition, String[] values, int index) { //TODO test
				return ((HeldItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation(condition.item.itemID))).getLocalizedName();
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
				return level_f.getInt(condition);
			}
		});
		evoParsers.put("move", new EvoParser<MoveCondition>(MoveCondition.class) {
			@Override
			public Object parse(MoveCondition condition, String[] values, int index) { //TODO test
				return AttackBase.getAttackBase(condition.attackIndex).map(attackBase -> (Object) attackBase.getLocalizedName()).orElse(PPConfig.noneText);
			}
		});
		evoParsers.put("movetype", new EvoParser<MoveTypeCondition>(MoveTypeCondition.class) {
			@Override
			public Object parse(MoveTypeCondition condition, String[] values, int index) throws IllegalAccessException {
				return ((EnumType) type_f.get(condition)).getLocalizedName();
			}
		});
//		evoParsers.put("partyalolan", new EvoParser<PartyAlolanCondition>(PartyAlolanCondition.class) {
//			@Override
//			public Object parse(PartyAlolanCondition condition, String[] values, int index) throws NoValueException, IllegalAccessException {
//				throw new NoValueException();
//			}
//		});
		evoParsers.put("party", new EvoParser<PartyCondition>(PartyCondition.class) {
			@Override
			public Object parse(PartyCondition condition, String[] values, int index) throws NoValueException {
				if (values.length > index) {
					if (values[index].equals("pokemon")) {
						return asReadableList(values, index + 1, condition.withPokemon.stream().map(EnumSpecies::getLocalizedName).toArray());
					} else if (values[index].equals("type")) {
						return asReadableList(values, index + 1, condition.withTypes.stream().map(EnumType::getLocalizedName).toArray());
					}
				}
				throw new NoValueException();
			}
		});
		//TODO StatRatioCondition
		evoParsers.put("time", new EvoParser<TimeCondition>(TimeCondition.class) { //TODO fix
			@Override
			public Object parse(TimeCondition condition, String[] values, int index) throws NoValueException {
				return normalizeText(condition.time.getLocalizedName());
			}
		});
		evoParsers.put("weather", new EvoParser<WeatherCondition>(WeatherCondition.class) {
			@Override
			public Object parse(WeatherCondition condition, String[] values, int index) throws IllegalAccessException {
				return normalizeText(((WeatherType) weather_f.get(condition)).getLocalizedName());
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
