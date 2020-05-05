package io.github.intransientmc.abstracter.emitter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static io.github.intransientmc.abstracter.Abstraction.EXPOSE_TYPE;
import static io.github.intransientmc.abstracter.util.Util.capitalizeFirstCharacter;

public class BaseEmitter implements Opcodes {
	public static void emitAbstractedMethod(ClassVisitor writer, String superClassName, String abstractClassName, String name, String abstractDesc, String vanillaDesc, boolean isStatic, boolean finalize, boolean exposed, boolean inter) {
		emitBridgeMethod(writer, abstractClassName, name, abstractDesc, vanillaDesc, isStatic, inter);
		int access = 0;
		if (exposed | inter) access |= ACC_PUBLIC;
		else access |= ACC_PROTECTED;
		if (finalize) access |= ACC_FINAL;
		if (isStatic) access |= ACC_STATIC;
		MethodVisitor visitor = writer.visitMethod(access, name, abstractDesc, null, null);
		createCall(visitor, superClassName, name, vanillaDesc, inter, INVOKESPECIAL);
	}

	public static void emitBridgeMethod(ClassVisitor cls, String abstractClassName, String name, String abstractDesc, String vanillaDesc, boolean isStatic, boolean inter) {
		int acc = ACC_PUBLIC | ACC_FINAL;
		if (isStatic) acc |= ACC_STATIC;
		MethodVisitor visitor = cls.visitMethod(acc, name, vanillaDesc, null, null);
		visitor.visitAnnotation(EXPOSE_TYPE, true); // strip it
		createCall(visitor, abstractClassName, name, abstractDesc, inter, INVOKEVIRTUAL);
	}

	public static void emitGetter(ClassVisitor cls, String owner, String name, String typeDesc, String abstractedDesc, boolean isStatic, boolean exposed) {
		int acc = ACC_FINAL;
		if (exposed) acc |= ACC_PUBLIC;
		else acc |= ACC_PROTECTED;
		if (isStatic) acc |= ACC_STATIC;
		MethodVisitor visitor = cls.visitMethod(acc, "get" + capitalizeFirstCharacter(name), "()" + typeDesc, null, null);
		visitor.visitCode();
		Type type = Type.getObjectType(typeDesc);
		if (isStatic) {
			visitor.visitFieldInsn(GETSTATIC, owner, name, typeDesc);
		} else {
			visitor.visitVarInsn(type.getOpcode(ILOAD), 0);
			visitor.visitFieldInsn(GETFIELD, owner, name, typeDesc);
		}
		visitor.visitTypeInsn(CHECKCAST, abstractedDesc);
		visitor.visitInsn(type.getOpcode(IRETURN));
		visitor.visitEnd();
	}

	public static void emitSetter(ClassVisitor cls, String owner, String name, String typeDesc, String abstractedDesc, boolean isStatic, boolean exposed) {
		int acc = ACC_FINAL;
		if (exposed) acc |= ACC_PUBLIC;
		else acc |= ACC_PROTECTED;
		if (isStatic) acc |= ACC_STATIC;
		MethodVisitor visitor = cls.visitMethod(acc, "set" + capitalizeFirstCharacter(name), "(" + abstractedDesc + ")V", null, null);
		visitor.visitCode();
		Type type = Type.getObjectType(typeDesc);
		if (!isStatic) visitor.visitVarInsn(type.getOpcode(ILOAD), 0);
		visitor.visitVarInsn(ALOAD, isStatic ? 0 : 1);
		visitor.visitTypeInsn(CHECKCAST, typeDesc);
		if (isStatic) visitor.visitFieldInsn(PUTSTATIC, owner, name, typeDesc);
		else visitor.visitFieldInsn(PUTFIELD, owner, name, typeDesc);

		visitor.visitEnd();
	}

	public static void createCall(MethodVisitor visitor, String className, String name, String desc, boolean inter, int insn) {
		if (inter) insn = INVOKEINTERFACE;
		visitor.visitCode();

		Type type = Type.getObjectType(desc);
		Type[] types = type.getArgumentTypes();
		for (int i = 0, length = types.length; i < length; i++) {
			Type arg = types[i];
			if (arg.getSort() == Type.OBJECT) visitor.visitTypeInsn(CHECKCAST, arg.getDescriptor());
			visitor.visitIntInsn(arg.getOpcode(ILOAD), i);
		}
		visitor.visitMethodInsn(insn, className, name, desc, inter);
		visitor.visitInsn(type.getReturnType().getOpcode(IRETURN));

		visitor.visitEnd();
	}
}
