package io.github.intransientmc.abstracter.util.remapper;

import io.github.intransientmc.abstracter.Classpath;

public class InterfaceClasspathRemapper extends PrefixingClasspathRemapper {
	public InterfaceClasspathRemapper(Classpath classpath) {
		super('I', classpath);
	}
}
