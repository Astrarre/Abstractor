package io.github.intransientmc.abstracter.util.remapper;

import io.github.intransientmc.abstracter.Classpath;
import io.github.intransientmc.abstracter.util.remapper.PrefixingClasspathRemapper;

public class ExceptionClasspathRemapper extends PrefixingClasspathRemapper {
	public ExceptionClasspathRemapper(Classpath classpath) {
		super('E', classpath);
	}
}
