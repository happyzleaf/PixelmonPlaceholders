package com.happyzleaf.pixelmonplaceholders.parser;

import com.happyzleaf.pixelmonplaceholders.parser.impl.*;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.entity.Entity;

import java.util.function.Function;

public class Parsers {
	public static Parser<Pokemon> pokemon = new PokemonParser();
	public static Parser<Pokemon> egg = new EggParser();

	public static Parser<Pair<EnumSpecies, IEnumForm>> species = new SpeciesParser();

	public static Parser<Function<StatsType, Integer>> stats = new StatsParser();

	public static Parser<Entity> party = new PartyParser();
}
