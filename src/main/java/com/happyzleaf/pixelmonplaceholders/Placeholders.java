package com.happyzleaf.pixelmonplaceholders;

import com.happyzleaf.pixelmonplaceholders.parser.Parsers;
import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import me.rojo8399.placeholderapi.*;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;

import javax.annotation.Nullable;

public class Placeholders {
	public boolean register(Object plugin) {
		PlaceholderService service = Sponge.getServiceManager().provide(PlaceholderService.class).orElse(null);
		if (service == null) {
			PixelmonPlaceholders.LOGGER.error("Couldn't load the placeholders! Are you sure that PlaceholderAPI is enabled?");
			return false;
		}

		try {
			for (ExpansionBuilder builder : service.loadAll(this, plugin)) {
				builder.author("happyzleaf").version(PixelmonPlaceholders.VERSION).buildAndRegister();
			}
		} catch (Exception e) {
			PixelmonPlaceholders.LOGGER.error("Couldn't load the placeholders.", e);
			return false;
		}

		return true;
	}

	@Placeholder(id = "party")
	public Object party(@Source Entity entity, @Nullable @Token String token) throws NoValueException {
		return Parsers.party.parse(entity, Args.of(token));
	}

	private static EnumSpecies testName(String name) {
		EnumSpecies species = EnumSpecies.getFromNameAnyCaseNoTranslate(name);
		if (species != null) {
			return species;
		}

		try {
			return EnumSpecies.getFromDex(Integer.parseInt(name));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Placeholder(id = "pokedex")
	public Object pokedex(@Token String token) throws NoValueException {
		Args args = Args.of(token);

		String arg = args.orElse(null);
		if (arg == null) {
			throw new NoValueException("You must specify the pokémon name or national id.");
		}

		EnumSpecies species = testName(arg);
		IEnumForm form = null;
		if (species == null) {
			int separator = arg.lastIndexOf('-');
			if (separator == -1) {
				throw new NoValueException(String.format("The pokémon '%s' was not recognized.", arg));
			}

			String name = arg.substring(0, separator);
			species = testName(name);
			if (species == null) {
				throw new NoValueException(String.format("The pokémon '%s' was not recognized.", name));
			}

			String suffix = arg.substring(separator);
			form = species.getPossibleForms(false).stream().filter(f -> f.getFormSuffix().equals(suffix)).findAny().orElse(null);
			if (form == null) {
				throw new NoValueException(String.format("The suffix '%s' was not recognized.", suffix));
			}

			if (form.isDefaultForm()) {
				form = null;
			}
		}

		return Parsers.species.parse(Pair.of(species, form), args);
	}
}
