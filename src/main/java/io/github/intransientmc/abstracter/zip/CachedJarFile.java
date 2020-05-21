package io.github.intransientmc.abstracter.zip;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CachedJarFile {
	private final Map<String, ClassNode> cache = new HashMap<>();
	private final ZipFile file;

	public CachedJarFile(ZipFile file) {this.file = file;}

	public ClassNode getClass(String className) {
		return this.cache.computeIfAbsent(className, c -> {
			try {
				ZipEntry entry = this.file.getEntry(c + ".class");
				if (entry == null) throw new ClassNotFoundException(c);
				InputStream stream = this.file.getInputStream(entry);
				ClassReader reader = new ClassReader(stream);
				ClassNode node = new ClassNode();
				reader.accept(node, 0);
				return node;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
	}
}
