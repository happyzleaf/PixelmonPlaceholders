package com.happyzleaf.pixelmonplaceholders.parser.impl;

import com.happyzleaf.pixelmonplaceholders.parser.ParserBase;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.happyzleaf.pixelmonplaceholders.utils.Utils;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import me.rojo8399.placeholderapi.NoValueException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StatsParser extends ParserBase<Function<StatsType, Integer>> {
	private static final Map<String, StatsType> STATS = Utils.populateMap(new HashMap<>(),
			"hp", StatsType.HP,
			"atk", StatsType.Attack,
			"def", StatsType.Defence,
			"spa", StatsType.SpecialAttack,
			"spd", StatsType.SpecialDefence,
			"spe", StatsType.Speed
	);

	public StatsParser() {
		super(STATS.keySet().toArray(new String[0]));
	}

	@Override
	public Object parse(Function<StatsType, Integer> obj, Args args) throws NoValueException {
		String token = args.orElse("");
		switch (token) {
			case "total":
				return STATS.values().stream().mapToInt(obj::apply).sum();
			case "percentage": {
				String maxToken = args.get();
				if (maxToken == null) {
					throw new NoValueException("You must specify the maximum of the stats to get the percentage.");
				}

				int max = parseInt(maxToken);
				if (max <= 0) {
					throw new NoValueException(String.format("The maximum must be positive. ('%d')", max));
				}

				return formatDouble(STATS.values().stream().mapToInt(obj::apply).sum() * 100.0 / max);
			}
			default:
				if (!STATS.containsKey(token)) {
					invalidArguments(token);
				}

				return obj.apply(STATS.get(token));
		}
	}
}
