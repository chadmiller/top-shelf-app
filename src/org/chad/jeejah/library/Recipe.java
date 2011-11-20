package org.chad.jeejah.library;

import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;

public class Recipe implements Comparable {

	public TreeSet<String> ingredients;
	public List<String> prepare_instructions;
	public List<String> consume_instructions;
	public String name;
	public String glass;

	public static final String KEY_PREPARE_INST = "prep";
	public static final String KEY_CONSUME_INST = "cons";
	public static final String KEY_NAME = "name";
	public static final String KEY_INGREDIENTS = "ingr";

	public Recipe() {
		this.ingredients = new TreeSet<String>();
		this.prepare_instructions = new LinkedList<String>();
		this.consume_instructions = new LinkedList<String>();
	}
	public Recipe(String name) {
		this.ingredients = new TreeSet<String>();
		this.prepare_instructions = new LinkedList<String>();
		this.consume_instructions = new LinkedList<String>();
		this.name = name;
	}

	public int compareTo(Object other) {
		if (this.name == null) {
			return 1;
		}
		return this.name.compareTo(((Recipe) other).name);
	}

	public String toString() {
		if (this.name == null) {
			return "(unnamed recipe)";
		}
		return this.name;
	}
}
