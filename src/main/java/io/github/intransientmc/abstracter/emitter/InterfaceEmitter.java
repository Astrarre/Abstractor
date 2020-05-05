package io.github.intransientmc.abstracter.emitter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import static io.github.intransientmc.abstracter.util.Util.capitalizeFirstCharacter;

public class InterfaceEmitter implements Opcodes {
	public static void emitInvoker(ClassVisitor cls, String name, String descriptor, boolean isStatic) {
		int acc = ACC_PUBLIC;
		if (isStatic) acc |= ACC_STATIC;
		cls.visitMethod(acc, name, descriptor, null, null);
	}

	public static void emitGetter(ClassVisitor cls, String name, String descriptor, boolean isStatic) {
		int access = ACC_PUBLIC;
		if (isStatic) access |= ACC_STATIC;
		cls.visitMethod(access, "get" + capitalizeFirstCharacter(name), "()"+descriptor, null, null);
	}

	public static void emitSetter(ClassVisitor cls, String name, String descriptor, boolean isStatic) {
		int access = ACC_PUBLIC;
		if (isStatic) access |= ACC_STATIC;
		cls.visitMethod(access, "set" + capitalizeFirstCharacter(name), "("+descriptor+")V", null, null);
	}
}
