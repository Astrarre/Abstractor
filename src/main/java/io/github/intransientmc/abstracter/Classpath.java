package io.github.intransientmc.abstracter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.github.intransientmc.abstracter.util.remapper.ExceptionClasspathRemapper;
import io.github.intransientmc.abstracter.util.remapper.InterfaceClasspathRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class Classpath {
	public final ExceptionClasspathRemapper exceptionRemapper = new ExceptionClasspathRemapper(this);
	public final InterfaceClasspathRemapper interfaceRemapper = new InterfaceClasspathRemapper(this);
	private final HashMap<String, ClassNode> nodes = new HashMap<>();
	private final Set<String> whitelist;
	private final ZipFile file;

	public Classpath(Set<String> whitelist, ZipFile file) {
		this.whitelist = whitelist;
		this.file = file;
	}

	/**
	 * @return null if the class is not in the zip
	 * @throws IllegalArgumentException if the class was not whitelisted for abstraction
	 */
	public ClassNode read(String cls) {
		return this.nodes.computeIfAbsent(cls, c -> {
			try {
				ZipEntry entry = this.file.getEntry(cls + ".class");
				if (entry != null) {
					if(this.whitelist.contains(c)) {
						InputStream input = this.file.getInputStream(entry);
						ClassReader reader = new ClassReader(input);
						ClassNode node = new ClassNode(Opcodes.ASM8);
						reader.accept(node, 0);
						return node;
					} else {
						throw new IllegalArgumentException(cls + " is not a whitelisted class");
					}
				}
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	public boolean canAbstract(String cls) {
		return this.nodes.containsKey(cls) || this.whitelist.contains(cls);
	}
}
