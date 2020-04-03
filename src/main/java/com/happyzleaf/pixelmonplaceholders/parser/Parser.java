package com.happyzleaf.pixelmonplaceholders.parser;

import com.happyzleaf.pixelmonplaceholders.parser.args.Args;
import me.rojo8399.placeholderapi.NoValueException;

public interface Parser<T> {
	Object parse(T obj, Args args) throws NoValueException;
}
