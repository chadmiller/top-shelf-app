package org.chad.jeejah.library;

import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;

final public class Recipe implements Comparable<Recipe> {

	final public TreeSet<String> ingredients;
	final public List<String> prepare_instructions;
	final public List<String> consume_instructions;
	public String name;
	public String glass;

	public static final String KEY_PREPARE_INST = "prep";
	public static final String KEY_CONSUME_INST = "cons";
	public static final String KEY_NAME = "name";
	public static final String KEY_INGREDIENTS = "ingr";
	public static final String KEY_GLASS = "glas";

	public Recipe() {
		this.ingredients = new TreeSet<String>();
		this.prepare_instructions = new LinkedList<String>();
		this.consume_instructions = new LinkedList<String>();
	}
	public Recipe(String name) {
		this();
		this.name = name;
	}

	public int compareTo(Recipe other) {
		if (this.name == null) {
			return 1;
		}
		return this.name.compareTo(other.name);
	}

	public String toString() {
		if (this.name == null) {
			return "(unnamed recipe)";
		}
		return this.name;
	}
}
