package io.github.intransientmc.abstracter.abstracter;

import static io.github.intransientmc.abstracter.util.Util.camelCase;

import java.lang.reflect.Modifier;

import io.github.intransientmc.abstracter.ClassAbstracter;
import io.github.intransientmc.abstracter.Classpath;
import io.github.intransientmc.abstracter.util.MethodProposal;
import io.github.intransientmc.abstracter.util.remapper.ExceptionClasspathRemapper;
import io.github.intransientmc.abstracter.util.remapper.InterfaceClasspathRemapper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractAbstracter implements ClassAbstracter {
	protected final ClassNode node;
	protected final Classpath classpath;

	protected AbstractAbstracter(ClassNode node, Classpath classpath) {
		this.node = node;
		this.classpath = classpath;
	}

	protected ExceptionClasspathRemapper getExceptionRemapper() {
		return this.classpath.exceptionRemapper;
	}

	protected InterfaceClasspathRemapper getInterfaceRemapper() {
		return this.classpath.interfaceRemapper;
	}


	/**
	 * copies the method descriptor and signature and everything else
	 *
	 * @return the methodvisitor, and the remapped descriptor
	 */
	public MethodProposal writeInvokerHeader(ClassWriter writer, MethodNode node) {
		String desc = this.getInterfaceRemapper().mapDesc(node.desc);
		// @formatter:off
		return new MethodProposal(() -> writer.visitMethod(node.access,
		                                  node.name,
		                                  desc,
		                                  this.getInterfaceRemapper().mapSignature(node.signature, false),
		                                  this.getExceptionRemapper().mapTypes(node.exceptions.toArray(new String[0]))
		), desc);
		// @formatter:on
	}

	public MethodProposal writeGetterHead(ClassWriter writer, String cls, FieldNode node) {
		String desc = "()"+this.getInterfaceRemapper().mapType(node.desc);
		// @formatter:off
		return new MethodProposal(() -> writer.visitMethod(node.access,
		                                           camelCase("get", node.name),
		                                           desc,
		                                           "()"+this.getInterfaceRemapper().mapSignature(node.signature, false),
		                                           null)
		, desc);
		// @formatter:on
	}

	/**
	 * @param cast true if `this` needs to be casted (only true for interface abstractions)
	 */
	public void writeGetter(MethodVisitor head, String cls, FieldNode node, boolean cast) {
		Type type = Type.getType(node.desc);
		if(!Modifier.isStatic(node.access)) {
			head.visitVarInsn(ALOAD, 0);
			if(cast) {
				head.visitTypeInsn(CHECKCAST, cls);
			}
			head.visitFieldInsn(GETFIELD, cls, node.name, node.desc);
		} else {
			head.visitFieldInsn(GETSTATIC, cls, node.name, node.desc);
		}

		// cast for return and to throw error
		if(this.classpath.canAbstract(node.desc)) {
			head.visitTypeInsn(CHECKCAST, type.getInternalName());
		}

		head.visitInsn(type.getOpcode(IRETURN));
	}

	public MethodProposal writeSetterHead(ClassWriter writer, FieldNode node) {
		String desc = '('+this.getInterfaceRemapper().mapType(node.desc)+")V";
		// @formatter:off
		return new MethodProposal(() -> writer.visitMethod(node.access,
		                                                   camelCase("set", node.name),
		                                                   desc,
		                                                   '('+this.getInterfaceRemapper().mapSignature(node.signature, false)+")V",
		                                                   null
		), desc);
		// @formatter:on
	}

	public void writeSetter(MethodVisitor head, String cls, FieldNode node, boolean cast) {
		Type type = Type.getType(node.desc);
		if(!Modifier.isStatic(node.access)) {
			head.visitVarInsn(ALOAD, 0);
			if(cast) {
				head.visitTypeInsn(CHECKCAST, cls);
			}
			head.visitVarInsn(type.getOpcode(ILOAD), 1);
			head.visitFieldInsn(PUTFIELD, cls, node.name, node.desc);
		} else {
			head.visitVarInsn(type.getOpcode(ILOAD), 0);
			head.visitFieldInsn(PUTSTATIC, cls, node.name, node.desc);
		}

		head.visitInsn(RETURN);
	}
}
