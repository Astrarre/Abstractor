package io.github.intransientmc.abstracter.util.remapper;

import io.github.intransientmc.abstracter.Classpath;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

public abstract class PrefixingClasspathRemapper extends Remapper {
	private final char prefix;
	private final Classpath classpath;

	public PrefixingClasspathRemapper(char prefix, Classpath classpath) {
		this.prefix = prefix;
		this.classpath = classpath;
	}

	@Override
	public String mapType(String internalName) {
		ClassNode node = this.classpath.read(internalName);
		// mc class, needs abstraction
		if(node != null) {
			String internal = node.superName;
			int substr = internal.lastIndexOf('/');
			return internal.substring(0, substr)+this.prefix+internal.substring(substr+1);
		}

		return super.mapType(internalName);
	}
}
