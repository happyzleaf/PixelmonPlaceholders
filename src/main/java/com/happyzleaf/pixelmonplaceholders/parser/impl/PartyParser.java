package com.happyzleaf.pixelmonplaceholders.parser.impl;

import com.happyzleaf.pixelmonplaceholders.parser.ParserBase;
import com.happyzleaf.pixelmonplaceholders.parser.Parsers;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import me.rojo8399.placeholderapi.NoValueException;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

public class PartyParser extends ParserBase<Entity> {
	public PartyParser() {
		super("1", "2", "3", "4", "5", "6");
	}

	@Override
	public Object parse(Entity obj, Args args) throws NoValueException {
		PartyStorage party;
		if (obj instanceof Player) {
			party = Pixelmon.storageManager.getParty(obj.getUniqueId());
		} else if (obj instanceof NPCTrainer) {
			party = ((NPCTrainer) obj).getPokemonStorage();
		} else {
			throw new NoValueException("The entity is not a player nor a trainer.");
		}

		int slot = minMax(parseInt(checkExists(args.get())), 1, PartyStorage.MAX_PARTY) - 1;
		Pokemon pokemon = party.get(slot);
		if (pokemon == null) {
			return "NONE"; // TODO
		}

		return Parsers.pokemon.parse(pokemon, args);
	}
}
