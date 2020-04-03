package com.happyzleaf.pixelmonplaceholders.parser.args;

import javax.annotation.Nullable;

public class Args {
	private final String[] args;
	private final int length;

	private int position;

	public Args(@Nullable String[] args) {
		this.args = args;
		this.length = args == null ? 0 : args.length;
		this.position = 0;
	}

	public boolean available() {
		return position < length;
	}

	/**
	 * This function returns the token at the current position,
	 * then proceeds to increment it.
	 *
	 * @return The token at the current position, or <code>value</code> if not available.
	 */
	public String orElse(String value) {
		return available() ? args[position++] : value;
	}

	/**
	 * {@link Args#orElse(String)}
	 */
	public String get() {
		return orElse(null);
	}

	/**
	 * Gets the previous token without modifying the position.
	 *
	 * @return The previous token.
	 */
	public String previous() {
		if (position == 0) {
			throw new IllegalStateException("There isn't any previous argument.");
		}

		return args[position - 1];
	}

	public static Args of(@Nullable String token) {
		return token == null ? new Args(null) : new Args(token.split("_"));
	}
}
