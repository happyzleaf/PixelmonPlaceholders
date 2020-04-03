package com.happyzleaf.pixelmonplaceholders.parser.impl;

import com.happyzleaf.pixelmonplaceholders.parser.ParserBase;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import me.rojo8399.placeholderapi.NoValueException;
import org.apache.commons.lang3.tuple.Pair;

public class SpeciesParser extends ParserBase<Pair<EnumSpecies, IEnumForm>> {
	public SpeciesParser() {
		super("", "name");
	}

	@Override
	public Object parse(Pair<EnumSpecies, IEnumForm> obj, Args args) throws NoValueException {
		EnumSpecies species = obj.getLeft();
		IEnumForm form = obj.getRight();

		switch (args.orElse("")) {
			case "":
			case "name":
				return translate(species.getUnlocalizedName());
			default:

		}

		return null;
	}
}
