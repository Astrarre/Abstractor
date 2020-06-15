package io.github.intransientmc.abstracter;

import jdk.nashorn.internal.codegen.types.Type;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public interface ClassAbstracter extends Opcodes {
	int INTERFACE_MASK = ~(ACC_ABSTRACT | ACC_ENUM | ACC_RECORD | ACC_INTERFACE);
	String OBJECT_NAME = Type.getInternalName(Object.class);
	ClassWriter emit();
}
