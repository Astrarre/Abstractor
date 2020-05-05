package io.github.intransientmc.abstracter.util;

import jdk.nashorn.internal.codegen.types.Type;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LazyClass {
	private static final String OBJECT_TYPE = Type.getInternalName(Object.class);
	public final String name;
	private final ZipFile file;
	private boolean ignore;
	private ClassNode node;

	public static LazyClass get(Map<String, LazyClass> cache, ZipFile file, String name) {
		return cache.computeIfAbsent(name, s -> new LazyClass(name, file));
	}

	public LazyClass(String name, ZipFile file) {
		this.name = name;
		this.file = file;
	}

	public boolean isInterface() {
		return Modifier.isInterface(this.resolve().access);
	}

	public ClassNode resolve() {
		if(this.node == null || !this.ignore) {
			ZipEntry entry = this.file.getEntry(this.name.replace('.', '/') + ".class");
			if(entry == null) {
				this.ignore = true;
				return null;
			}
			try {
				InputStream stream = this.file.getInputStream(entry);
				ClassReader reader = new ClassReader(stream);
				ClassNode node = new ClassNode();
				reader.accept(node, 0);
				this.node = node;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return this.node;
	}

	public Collection<LazyClass> getClasses(Map<String, LazyClass> cache) throws ClassNotFoundException {
		Set<LazyClass> classes = new HashSet<>();
		this.find(classes, cache);
		return classes;
	}

	private void find(Set<LazyClass> supers, Map<String, LazyClass> cache) {
		supers.add(this);
		ClassNode node = this.resolve();
		if(node == null)
			return; // unable to find class

		for (String inter : node.interfaces) {
			LazyClass cls = cache.computeIfAbsent(inter, s -> new LazyClass(s, this.file));
			if(!supers.contains(cls)) {
				cls.find(supers, cache);
			}
		}

		String superName = node.superName;
		if(!OBJECT_TYPE.equals(superName)) {
			LazyClass cls = cache.computeIfAbsent(superName, s -> new LazyClass(s, this.file));
			cls.find(supers, cache);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || this.getClass() != o.getClass()) return false;

		LazyClass lazyClass = (LazyClass) o;

		return this.name.equals(lazyClass.name);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
