package com.happyzleaf.pixelmonplaceholders.parser.impl;

import com.happyzleaf.pixelmonplaceholders.parser.ParserBase;
import com.happyzleaf.pixelmonplaceholders.parser.Parsers;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.rojo8399.placeholderapi.NoValueException;
import org.apache.commons.lang3.tuple.Pair;

public class PokemonParser extends ParserBase<Pokemon> {
	public PokemonParser() {
		super("", "nickname", "species");
	}

	@Override
	public Object parse(Pokemon obj, Args args) throws NoValueException {
		switch (args.orElse("")) {
			case "":
			case "nickname":
				return obj.getDisplayName();
			case "species":
				return Parsers.species.parse(Pair.of(obj.getSpecies(), obj.getFormEnum()), args);
			default:
				return invalidArguments(args.previous());
		}
	}
}
