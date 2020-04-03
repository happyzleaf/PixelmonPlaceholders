package com.happyzleaf.pixelmonplaceholders.parser;

import com.happyzleaf.pixelmonplaceholders.parser.impl.PartyParser;
import com.happyzleaf.pixelmonplaceholders.parser.impl.PokemonParser;
import com.happyzleaf.pixelmonplaceholders.parser.impl.SpeciesParser;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.entity.Entity;

public class Parsers {
	public static Parser<Pokemon> pokemon = new PokemonParser();
	public static Parser<Pair<EnumSpecies, IEnumForm>> species = new SpeciesParser();
	public static Parser<Entity> party = new PartyParser();
}
