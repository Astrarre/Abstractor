package io.github.intransientmc.abstracter.interfaces;

import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.intransientmc.abstracter.Classpath;
import io.github.intransientmc.abstracter.abstracter.AbstractAbstracter;
import io.github.intransientmc.abstracter.util.MethodProposal;
import io.github.intransientmc.abstracter.util.Pair;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class InterfaceAbstracter extends AbstractAbstracter {
	public InterfaceAbstracter(ClassNode node, Classpath classpath) {
		super(node, classpath);
	}

	@Override public ClassWriter emit() {
		ClassWriter writer = new ClassWriter(0);
		List<String> supers = this.node.interfaces;
		// todo check if class path has it
		if (this.classpath.canAbstract(this.node.superName)) {
			supers.add(this.node.superName);
		}

		writer.visit(V1_8,
		             (this.node.access & INTERFACE_MASK) | ACC_INTERFACE,
		             this.getInterfaceRemapper().mapType(this.node.name),
		             this.getInterfaceRemapper().mapType(this.node.signature),
		             OBJECT_NAME,
		             this.getInterfaceRemapper().mapTypes(supers.toArray(new String[0]))
		);

		Set<String> signatures = new HashSet<>();
		// todo try catch to skip methods
		// invoker implementation
		for (MethodNode method : this.node.methods) {
			// only public methods are exposed by interface abstractions
			if(Modifier.isPublic(method.access)) {
				MethodProposal pair = this.writeInvokerHeader(writer, method);
				MethodVisitor node = pair.makeVisitor();
				String desc = pair.getDesc();
				signatures.add(method.name + ';' + desc);
				// if non-primitive
				if (!desc.equals(method.desc)) {
					node.visitCode();
					Type methodDesc = Type.getMethodType(desc);
					int index = 0;
					if (!isStatic(method.access)) {
						index = 1; // to load this
						// load and cast `this`
						node.visitVarInsn(ALOAD, 0);
						node.visitTypeInsn(CHECKCAST, this.node.name);
					}

					for (Type type : methodDesc.getArgumentTypes()) {
						node.visitVarInsn(type.getOpcode(ILOAD), index++);
					}

					node.visitMethodInsn();

					node.visitInsn(methodDesc.getReturnType().getOpcode(IRETURN));
					node.visitEnd();
				}
			}
		}

		// getters / setters
		for (FieldNode field : this.node.fields) {
			if(Modifier.isPublic(field.access)) {

			}
		}


		return null;
	}
}
