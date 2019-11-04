package com.github.happyzleaf.pixelmonplaceholders;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
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
				case "team": // remove? %party% is enough
					if (values.length > 1) {
						int slot;
						try {
							slot = Integer.parseInt(values[1]) - 1;
						} catch (NumberFormatException e) {
							throw new NoValueException(String.format("%s is not a valid number.", values[0]));
						}
						if (slot < 0 || slot >= PartyStorage.MAX_PARTY) {
							throw new NoValueException(String.format("The slot must be between 1 and %d.", PartyStorage.MAX_PARTY));
						}
						
						return parsePokemonInfo(player, party.get(slot), copyOfRange(values, 2, values.length));
					}
					break;
				case "dexcount":
					return party.pokedex.countCaught();
				case "dexpercentage":
					return formatDouble(party.pokedex.countCaught() * 100 / (double) EnumSpecies.values().length);
				case "seencount":
					return party.pokedex.getSeenMap().size();
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
		
		int slot;
		try {
			slot = Integer.parseInt(values[0]) - 1;
		} catch (IndexOutOfBoundsException e) {
			throw new NoValueException("You didn't provide the party slot.");
		} catch (NumberFormatException e) {
			throw new NoValueException("%s is not a valid number.");
		}
		if (slot < 0 || slot >= PlayerPartyStorage.MAX_PARTY) {
			throw new NoValueException(String.format("The party slot must be between 1 and %d.", PlayerPartyStorage.MAX_PARTY));
		}
		
		return parsePokemonInfo(entity, storage.get(slot), copyOfRange(values, 1, values.length));
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
		String[] values = token.toLowerCase().split("_");
		if (values.length > 0) {
			switch (values[0]) {
				case "dexsize":
					return EnumSpecies.values().length;
				case "nextlegendary":
					return TimeUnit.MILLISECONDS.toSeconds(PixelmonSpawning.legendarySpawner.nextSpawnTime - System.currentTimeMillis());
			}
		}
		
		throwWrongInput("dexsize", "nextlegendary");
		return null;
	}
	
	@Placeholder(id = "pokedex")
	public Object pokedex(@Token String token) throws NoValueException {
		String[] values = token.split("_");
		if (values.length > 0) {
			EnumSpecies pokemon = null;
			
			try {
				int nationalId = Integer.parseInt(values[0]);
				if (nationalId >= 0 && nationalId <= EnumSpecies.values().length) {
					pokemon = EnumSpecies.getFromNameAnyCase(Pokedex.fullPokedex.get(nationalId).name);
				}
			} catch (NumberFormatException e) {
				pokemon = EnumSpecies.getFromNameAnyCase(values[0]);
			}
			
			if (pokemon != null) {
				if (values.length > 1) {
					IEnumForm form = null;
					for (IEnumForm f : EnumSpecies.formList.get(pokemon)) {
						if (((Enum) f).name().toLowerCase().equals(values[1])) {
							form = f;
							break;
						}
					}
					if (form != null || values[1].equals("normal")) {
						return parsePokedexInfo(pokemon, form, copyOfRange(values, 2, values.length));
					}
				}
				return parsePokedexInfo(pokemon, null, copyOfRange(values, 1, values.length));
			}
		} else {
			throw new NoValueException("Not enough arguments.");
		}
		throw new NoValueException();
	}
}
