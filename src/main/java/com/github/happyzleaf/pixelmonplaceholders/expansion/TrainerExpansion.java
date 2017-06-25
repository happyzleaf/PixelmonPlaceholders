package com.github.happyzleaf.pixelmonplaceholders.expansion;

import com.github.happyzleaf.pixelmonplaceholders.utility.ParserUtility;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Stats;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/***************************************
 * PixelmonPlaceholders
 * Created on 29/05/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public class TrainerExpansion extends ExpansionBase {
	@Override
	public String getIdentifier() {
		return "trainer";
	}
	
	@Override
	public String getDescription() {
		return "Pixelmon trainer's Placeholders.";
	}
	
	@Override
	public List<String> getSupportedTokens() {
		return Arrays.asList("dexcount", "dexpercentage", "seencount", "wins", "losses", "wlratio", "balance", "team-[position]");
	}
	
	@Override
	public Object onValueRequest(Player player, Optional<String> token) {
		Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) player);
		if (token.isPresent() && optStorage.isPresent()) {
			PlayerStorage storage = optStorage.get();
			String[] values = token.get().split("_");
			switch (values[0]) {
				case "dexcount":
					return storage.pokedex.countCaught();
				case "dexpercentage":
					String result1 = String.valueOf((double) storage.pokedex.countCaught() * 100 / EnumPokemon.values().length);
					if (result1.substring(result1.indexOf(".") + 1).length() == 1) {
						return result1.substring(0, result1.length() - 2);
					} else {
						return result1.substring(0, result1.indexOf(".") + 3);
					}
				case "seencount":
					return storage.pokedex.getSeenMap().size();
				case "wins":
					return storage.stats.getWins();
				case "losses":
					return storage.stats.getLosses();
				case "wlratio":
					int wins = storage.stats.getWins();
					int total = wins + storage.stats.getLosses();
					double result2;
					if (total <= 0) {
						result2 = 1;
					} else {
						result2 = (double) wins / total;
					}
					if (String.valueOf(result2).substring(String.valueOf(result2).indexOf(".") + 1).length() == 1) {
						return String.valueOf(result2).concat("0");
					} else {
						return String.valueOf(result2).substring(0, String.valueOf(result2).indexOf(".") + 3);
					}
				case "balance":
					return ParserUtility.formatBigNumbers(storage.getCurrency());
				case "team":
					if (values.length > 1) {
						String[] pokeValues = Arrays.copyOfRange(values, 1, values.length);
						try {
							EntityPixelmon pokemon = storage.getPokemon(storage.getIDFromPosition(Integer.parseInt(pokeValues[0]) - 1), (World) player.getWorld());
							if (pokemon == null) {
								return null;
							}
							
							if (pokeValues.length >= 2) {
								switch (pokeValues[1]) {
									case "nickname":
										return pokemon.hasNickname() ? pokemon.getNickname() : pokemon.getName();
									case "exp":
										return ParserUtility.formatBigNumbers(pokemon.getLvl().getExp());
									case "level":
										return pokemon.getLvl().getLevel();
									case "exptolevelup":
										return ParserUtility.formatBigNumbers(pokemon.getLvl().getExpForNextLevelClient());
									case "stats":
										if (pokeValues.length >= 3) {
											Stats stats = pokemon.stats;
											switch (pokeValues[2]) {
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
													if (pokeValues.length >= 4) {
														IVStore ivs = stats.IVs;
														switch (pokeValues[3]) {
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
														}
													}
													break;
												case "evs":
													if (pokeValues.length >= 4) {
														EVsStore evs = stats.EVs;
														switch (pokeValues[3]) {
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
														}
													}
													break;
											}
										}
										break;
									case "helditem":
										return pokemon.heldItem == null ? "None" : pokemon.heldItem.getDisplayName();
									case "pos":
										if (pokeValues.length >= 3) {
											BlockPos pos = pokemon.getPosition();
											switch (pokeValues[2]) {
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
											return moveset.get(Integer.parseInt(pokeValues[2]) - 1);
										} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
											return ParserUtility.asReadableList(pokeValues, 2, moveset.attacks);
										}
									case "friendship":
										return ParserUtility.formatBigNumbers(pokemon.friendship.getFriendship());
									case "ability":
										if (pokeValues.length == 2) {
											return pokemon.getAbility().getName();
										}
										break;
									case "ball":
										return pokemon.caughtBall.name();
								}
							}
							
							return ParserUtility.parsePokedexInfo(EnumPokemon.getFromNameAnyCase(pokemon.getPokemonName()), pokeValues);
						} catch (NumberFormatException ignored) {
						}
					}
					break;
			}
		}
		return null;
	}
}
