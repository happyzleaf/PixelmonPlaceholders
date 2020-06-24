package com.happyzleaf.pixelmonplaceholders.parser.impl;

import com.happyzleaf.pixelmonplaceholders.parser.ParserBase;
import com.happyzleaf.pixelmonplaceholders.parser.Parsers;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.rojo8399.placeholderapi.NoValueException;
import org.apache.commons.lang3.tuple.Pair;

public class PokemonParser extends ParserBase<Pokemon> {
	public PokemonParser() {
		super("", "nickname", "species", "stats", "exp", "level", "exptolevelup");
	}

	@Override
	public Object parse(Pokemon obj, Args args) throws NoValueException {
		if (obj.isEgg()) {
			return Parsers.egg.parse(obj, args);
		}

		switch (args.orElse("")) {
			case "":
			case "nickname":
				return obj.getDisplayName();
			case "species":
				return Parsers.species.parse(Pair.of(obj.getSpecies(), obj.getFormEnum()), args);
			case "stats": {
				switch (args.orElse("")) {
					case "evs":
						return Parsers.stats.parse(obj.getStats().evs::get, args);
					case "ivs":
						return Parsers.stats.parse(obj.getStats().ivs::get, args);
					default:
						return Parsers.stats.parse(obj.getStats()::get, args);
				}
			}
			case "exp":
				return formatBigNumber(obj.getExperience());
			case "level":
				return obj.getLevel();
			case "exptolevelup":
				return formatBigNumber(obj.getExperienceToLevelUp());
			default:
				return invalidArguments(args);
		}
	}
}
