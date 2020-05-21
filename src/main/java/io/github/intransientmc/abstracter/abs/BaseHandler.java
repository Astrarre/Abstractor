package io.github.intransientmc.abstracter.abs;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class BaseHandler extends AbstractionHandler {
	// the names of all the interfaces that were extended
	private final Set<String> extensible = new HashSet<>();
	// the names of all the interfaces that were in method parameters
	public final Set<String> dependancies = new HashSet<>();
	// base classes that need to be abstracted (super classes)
	public final Set<String> todo;
	// base classes that have been abstracted
	public final Set<String> done = new HashSet<>();

	public BaseHandler(ZipFile minecraft, Set<String> todo) {
		super(minecraft);
		this.todo = todo;
	}

	public void process() {
		while (!this.todo.isEmpty()) {
			Iterator<String> iterator = new ArrayList<>(this.todo).iterator();
			this.todo.clear();
			while (iterator.hasNext()) {
				String s = iterator.next();
				// todo process
				iterator.remove();
				this.done.add(s);
			}
		}
	}

	private void add(String name) {
		if(!this.done.contains(name)) this.todo.add(name);
	}

	protected ClassNode head(ClassNode target) {
		List<String> interfaces = new ArrayList<>(target.interfaces);
		for (int i = 0; i < interfaces.size(); i++) {
			String get = interfaces.get(i);
			this.extensible.add(get);
			interfaces.set(i, interfaceName(get));
		}
		ClassNode node = new ClassNode();
		node.interfaces = interfaces;
		node.access = target.access;
		// todo class signatures
		if(target.signature != null)
			System.out.println(target.signature);

		node.name = baseName(target.name);
		node.superName = baseName(target.superName);
		// make sure to abstract super classes
		this.add(target.superName);
		Set<String> methodDesc = new HashSet<>();


		// todo methods
		// todo fields

		return node;
	}


}
