package com.happyzleaf.pixelmonplaceholders.parser.impl;

import com.happyzleaf.pixelmonplaceholders.parser.ParserBase;
import com.happyzleaf.pixelmonplaceholders.parser.Parsers;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import me.rojo8399.placeholderapi.NoValueException;
import org.apache.commons.lang3.tuple.Pair;

public class SpeciesParser extends ParserBase<Pair<EnumSpecies, IEnumForm>> {
	public SpeciesParser() {
		super("", "name", "stats");
	}

	@Override
	public Object parse(Pair<EnumSpecies, IEnumForm> obj, Args args) throws NoValueException {
		EnumSpecies species = obj.getLeft();
		IEnumForm form = obj.getRight();
		BaseStats stats = species.getBaseStats(form);

		switch (args.orElse("")) {
			case "":
			case "name":
				return translate(species.getUnlocalizedName());
			case "stats":
				switch (args.orElse("")) {
					case "yield":
						return Parsers.stats.parse(stats.evYields::get, args);
					default:
						return Parsers.stats.parse(stats::get, args);
				}
			default:
		}

//		species.getBaseStats().get()

		return null;
	}
}
