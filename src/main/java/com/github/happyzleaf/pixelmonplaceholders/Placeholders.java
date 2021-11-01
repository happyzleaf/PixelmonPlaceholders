package com.github.happyzleaf.pixelmonplaceholders;

import com.github.happyzleaf.pixelmonplaceholders.utility.RayTracingHelper;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.pokedex.Pokedex;
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import me.rojo8399.placeholderapi.*;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

import static com.github.happyzleaf.pixelmonplaceholders.utility.ParserUtility.*;

public class Placeholders {
	public static void register() {
		Sponge.getServiceManager().provideUnchecked(PlaceholderService.class).loadAll(new Placeholders(), PixelmonPlaceholders.instance).stream().map(builder -> {
			switch (builder.getId()) { // TODO update?
				case "trainer":
					return builder.tokens("dexcount", "dexpercentage", "seencount", "wins", "losses", "wlratio", "balance", "team-[position]").description("Pixelmon trainer's Placeholders.");
				case "pixelmon":
					return builder.tokens("dexsize", "dexsizeall").description("General Pixelmon's placeholders.");
				case "pokedex":
					return builder.tokens("[name]", "[nationalId]").description("Specific Pokémon's placeholders.");
			}
			return builder;
		}).map(builder -> builder.author("happyzleaf").plugin(PixelmonPlaceholders.instance).version(PixelmonPlaceholders.VERSION)).forEach(builder -> {
			try {
				builder.buildAndRegister();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	@Placeholder(id = "trainer")
	public Object trainer(@Source Player player, @Token String token) throws NoValueException {
		PlayerPartyStorage party = Pixelmon.storageManager.getParty((EntityPlayerMP) player);
		String[] values = token.split("_");
		if (values.length > 0) {
			switch (values[0]) {
				case "dexcount":
					return party.pokedex.countCaught();
				case "dexpercentage":
					return formatDouble(party.pokedex.countCaught() * 100 / (double) Pokedex.pokedexSize);
				case "seencount":
					return party.pokedex.countSeen();
				case "wins":
					return party.stats.getWins();
				case "losses":
					return party.stats.getLosses();
				case "wlratio": {
					return formatDouble((double) party.stats.getWins() / Math.max(party.stats.getLosses(), 1));
				}
				case "balance":
					return formatBigNumbers(party.getMoney());
			}
		} else {
			throw new NoValueException("Not enough arguments.");
		}
		throw new NoValueException();
	}

	@Placeholder(id = "party")
	public Object party(@Source Entity entity, @Token String token) throws NoValueException {
		PartyStorage storage;
		if (entity instanceof EntityPlayerMP) {
			storage = Pixelmon.storageManager.getParty((EntityPlayerMP) entity);
		} else if (entity instanceof NPCTrainer) {
			storage = ((NPCTrainer) entity).getPokemonStorage();
		} else {
			throw new NoValueException("The source must be a Player or a NPCTrainer.");
		}

		String[] values = token.split("_");
		if (values.length == 0) {
			throw new NoValueException("Missing party slot. [1-6]");
		}

		int slot;
		try {
			slot = Integer.parseInt(values[0]) - 1;
		} catch (NumberFormatException e) {
			throw new NoValueException("%s is not a valid number.");
		}
		if (slot < 0 || slot >= PlayerPartyStorage.MAX_PARTY) {
			throw new NoValueException(String.format("The party slot must be between 1 and %d.", PlayerPartyStorage.MAX_PARTY));
		}

		return parsePokemonInfo(entity, storage.get(slot), copyOfRange(values, 1, values.length));
	}

	@Placeholder(id = "raytrace")
	public Object rayTrace(@Source Player player, @Token @Nullable String token) throws NoValueException {
		net.minecraft.entity.Entity hit = RayTracingHelper.getLookedEntity((net.minecraft.entity.Entity) player, PPConfig.rayTraceDistance).orElse(null);
		if (!(hit instanceof EntityPixelmon)) {
			return PPConfig.noneText;
		}

		return parsePokemonInfo(player, ((EntityPixelmon) hit).getPokemonData(), token == null ? new String[0] : token.split("_"));
	}

	@Placeholder(id = "pixelmon")
	public Object pixelmon(@Token String token) throws NoValueException {
		String[] values = token.toLowerCase().split("_");
		if (values.length > 0) {
			switch (values[0]) {
				case "dexsize":
					return EnumSpecies.values().length;
				case "nextlegendary":
					return TimeUnit.MILLISECONDS.toSeconds(PixelmonSpawning.legendarySpawner.nextSpawnTime - System.currentTimeMillis());
				case "nextmegaboss":
					return TimeUnit.MILLISECONDS.toSeconds(PixelmonSpawning.megaBossSpawner.nextSpawnTime - System.currentTimeMillis());
			}
		}

		return throwWrongInput("dexsize", "nextlegendary", "nextmegaboss");
	}

	private static EnumSpecies testName(String name) {
		EnumSpecies species = EnumSpecies.getFromNameAnyCaseNoTranslate(name);
		if (species != null) {
			return species;
		}

		try {
			return EnumSpecies.getFromDex(Integer.parseInt(name));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Placeholder(id = "pokedex")
	public Object pokedex(@Token String token) throws NoValueException {
		String[] values = token.split("_");
		if (values.length == 0) {
			throw new NoValueException("Not enough arguments.");
		}

		EnumSpecies species = testName(values[0]);
		IEnumForm form = null;
		if (species == null) {
			int separator = values[0].lastIndexOf('-');
			if (separator == -1) {
				throw new NoValueException(String.format("The pokémon '%s' was not recognized.", values[0]));
			}

			String name = values[0].substring(0, separator);
			species = testName(name);
			if (species == null) {
				throw new NoValueException(String.format("The pokémon '%s' was not recognized.", name));
			}

			String suffix = values[0].substring(separator);
			form = species.getPossibleForms(false).stream().filter(f -> f.getFormSuffix().equals(suffix)).findAny().orElse(null);
			if (form == null) {
				throw new NoValueException(String.format("The suffix '%s' was not recognized.", suffix));
			}

			if (form.isDefaultForm()) {
				form = null;
			}
		}

		return parsePokedexInfo(species, form, copyOfRange(values, 1, values.length));
	}
}
