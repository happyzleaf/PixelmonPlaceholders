package com.happyzleaf.pixelmonplaceholders.utils;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class Utils {
	public static <M extends Map<K, V>, K, V> M populateMap(M map, Object... args) {
		checkArgument(args.length % 2 == 0, "The args must be even.");

		for (int i = 0; i < args.length; i += 2) {
			map.put((K) args[i], (V) args[i + 1]);
		}

		return map;
	}
}
