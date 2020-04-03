package com.happyzleaf.pixelmonplaceholders.parser;

import com.google.common.collect.ImmutableList;
import com.pixelmonmod.pixelmon.util.ITranslatable;
import me.rojo8399.placeholderapi.NoValueException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.translation.Translation;

import java.util.List;

public abstract class ParserBase<T> implements Parser<T> {
	protected final List<String> keys;

	protected ParserBase(String... keys) {
		this.keys = ImmutableList.copyOf(keys);
	}

	/**
	 * TODO
 	 */
	protected String translate(String key) throws NoValueException {
		if (true) {
			return key;
		}

		Player observer = null; // get the observer somehow

		Translation translation = Sponge.getRegistry().getTranslationById(key).orElse(null);
		if (translation == null) {
			throw new NoValueException(String.format("Translation not valid. ('%s')", key));
		}

		return translation.get(observer.getLocale());
	}

	protected String translate(ITranslatable translatable) throws NoValueException {
		return translate(translatable.getUnlocalizedName());
	}

	protected Object invalidArguments(String arg) throws NoValueException {
		throw new NoValueException(String.format("Argument not valid. ('%s')", arg), keys);
	}

	protected Object notEnoughArguments() throws NoValueException {
		throw new NoValueException("Not enough arguments.", keys);
	}

	protected String checkExists(String arg) throws NoValueException {
		if (arg == null) {
			notEnoughArguments();
		}

		return arg;
	}

	protected int parseInt(String integer) throws NoValueException {
		try {
			return Integer.parseInt(integer);
		} catch (NumberFormatException e) {
			throw new NoValueException(String.format("'%s' is not an integer.", integer));
		}
	}

	protected int minMax(int integer, int min, int max) throws NoValueException {
		if (integer < min || integer > max) {
			throw new NoValueException(String.format("The integer must be between %d and %d. ('%d')", min, max, integer));
		}

		return integer;
	}
}
