package io.github.intransientmc.abstracter;

import org.objectweb.asm.Opcodes;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

// for merger: add AWs
public class Abstraction implements Opcodes {
	// usage: java -jar yarn_minecraft.jar config.csv

	// interfaces that are implemented by classes have to be non-final
	// interfaces that are just used by classes can be final
	// super classes have to be abstracted

	public static void main(String[] args) throws IOException {
		File minecraft = new File(args[0]);
		Set<String> base = start(args[1]);
		Set<String> inter = start(args[2]);
	}

	private static Set<String> start(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Set<String> set = new HashSet<>();
		reader.lines().reduce((a, b) -> a + ',' + b).ifPresent(s -> {
			int last = -1;
			int next;
			while ((next = s.indexOf(',', last+1)) != -1) {
				set.add(s.substring(last+1, next).replace('.', '/'));
				last = next;
			}
		});
		return set;
	}
}