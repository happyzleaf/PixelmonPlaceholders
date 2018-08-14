package com.github.happyzleaf.pixelmonplaceholders;

import static com.github.happyzleaf.pixelmonplaceholders.utility.ParserUtility.formatBigNumbers;
import static com.github.happyzleaf.pixelmonplaceholders.utility.ParserUtility.parsePokedexInfo;
import static com.github.happyzleaf.pixelmonplaceholders.utility.ParserUtility.parsePokemonInfo;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.pokedex.Pokedex;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import me.rojo8399.placeholderapi.Listening;
import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Arrays;
import java.util.Optional;

@Listening
public class Placeholders {
	@Placeholder(id = "trainer")
	public Object trainer(@Source Entity playerOrTrainer, @Token String token) throws NoValueException {
		Optional<PlayerStorage> optStorage;
		if (playerOrTrainer instanceof EntityPlayerMP) {
			optStorage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) playerOrTrainer);
		} else if (playerOrTrainer instanceof NPCTrainer) {
			optStorage = Optional.of(((NPCTrainer) playerOrTrainer).getPokemonStorage());
		} else {
			throw new NoValueException();
		}

		if (optStorage.isPresent()) {
			PlayerStorage storage = optStorage.get();
			String[] values = token.split("_");
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
					return formatBigNumbers(storage.getMoney());
				case "team": //TODO move to %party_[...]%
					if (values.length > 1) {
						String[] pokeValues = Arrays.copyOfRange(values, 1, values.length);
						try {
							return parsePokemonInfo(playerOrTrainer, storage, storage.getIDFromPosition(Integer.parseInt(pokeValues[0]) - 1), pokeValues);
						} catch (NumberFormatException ignored) {}
					}
					break;
			}
		}
		throw new NoValueException();
	}

	//don't mind me
	/*@Placeholder(id = "ray")
	public Object ray(@Source Player player, @Token String token) throws NoValueException {
		String[] values = token.split("_");

		Optional<BlockRayHit<org.spongepowered.api.world.World>> hit = BlockRay
				.from(player)
				.stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1))
				.distanceLimit(values.length == 0 ? 10 : Integer.parseInt(values[0]))
				.build()
				.end();
		if (!hit.isPresent()) {

		}
	}*/

	@Placeholder(id = "pixelmon")
	public Object pixelmon(@Token String token) throws NoValueException {
		switch (token) {
			case "dexsize":
				return EnumPokemon.values().length;
			case "dexsizeall":
				return Pokedex.pokedexSize;
		}
		throw new NoValueException();
	}

	@Placeholder(id = "pokedex")
	public Object pokedex(@Token String token) throws NoValueException {
		String[] values = token.split("_");
		if (values.length >= 1) {
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
				return parsePokedexInfo(pokemon, values);
			}
		}
		throw new NoValueException();
	}
}
