package com.happyzleaf.pixelmonplaceholders.parser.impl;

import com.happyzleaf.pixelmonplaceholders.parser.ParserBase;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import me.rojo8399.placeholderapi.NoValueException;

public class EggParser extends ParserBase<Pokemon> {
	public EggParser() {
		super("texturelocation");
	}

	@Override
	public Object parse(Pokemon obj, Args args) throws NoValueException {
		if (!obj.isEgg()) {
			throw new NoValueException(String.format("The pokémon '%s' is not an egg.", obj.getSpecies().getPokemonName()));
		}

		if (args.orElse("").equals("texturelocation")) {
			return "pixelmon:sprites/eggs/"
					+ (obj.getSpecies() == EnumSpecies.Togepi ? "togepi" : obj.getSpecies() == EnumSpecies.Manaphy ? "manaphy" : "egg")
					+ (obj.getEggCycles() > 10 ? "1" : obj.getEggCycles() > 5 ? "2" : "3");
		}

		return invalidArguments();
	}
}
