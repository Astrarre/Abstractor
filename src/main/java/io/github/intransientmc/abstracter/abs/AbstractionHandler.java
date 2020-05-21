package io.github.intransientmc.abstracter.abs;

import io.github.intransientmc.abstracter.util.SUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SignatureRemapper;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AbstractionHandler implements Opcodes {
	// the minecraft zip
	protected final ZipFile minecraft;

	public AbstractionHandler(ZipFile minecraft) {this.minecraft = minecraft;}

	protected ClassNode read(String name) throws IOException {
		ZipEntry entry = this.minecraft.getEntry(name + ".class");
		if (entry == null) return null;
		ClassNode node = new ClassNode();
		InputStream stream = this.minecraft.getInputStream(entry);
		ClassReader reader = new ClassReader(stream);
		reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
		stream.close();
		return node;
	}

	protected static MethodNode generateGetter(String name, FieldNode field, boolean cast) {
		MethodNode node = new MethodNode(field.access & (15)/*magic constant for access + static*/ | ACC_FINAL, "get" + SUtil.capitalize(field.name), remapSigToInterface("()" + field.desc), remapSigToInterface("()" + field.signature), null);
		if((field.access & ACC_STATIC) == 0) {
			node.visitVarInsn(ALOAD, 0);
			if(cast) {
				node.visitTypeInsn(CHECKCAST, name);
			}
			node.visitFieldInsn(GETFIELD, name, field.name, field.desc);
		} else {
			node.visitFieldInsn(GETSTATIC, name, field.name, field.desc);
		}
		node.visitInsn(Type.getType(field.desc).getOpcode(IRETURN));
		return node;
	}

	protected static MethodNode generateSetter(String name, FieldNode field, boolean cast) {
		MethodNode node = new MethodNode(field.access & (15)/*magic constant for access + static*/ | ACC_FINAL, "set" + SUtil.capitalize(field.name), remapSigToInterface("(" + field.desc + ")V"), remapSigToInterface("(" + field.signature + ")V"), null);
		Type ret = Type.getType(field.desc);
		if((field.access & ACC_STATIC) == 0) {
			node.visitVarInsn(ALOAD, 0);
			if(cast) {
				node.visitTypeInsn(CHECKCAST, name);
			}
			node.visitVarInsn(ret.getOpcode(ILOAD), 1);
			node.visitFieldInsn(PUTFIELD, name, field.name, field.desc);
		} else {
			node.visitVarInsn(ret.getOpcode(ILOAD), 0);
			node.visitFieldInsn(PUTSTATIC, name, field.name, field.desc);
		}
		node.visitInsn(RETURN);
		return node;
	}

	protected MethodNode spitInvoker(MethodNode node, String ownerName, boolean cast) {
		MethodNode base = new MethodNode(node.access, node.name, remapSigToInterface(node.desc), remapSigToInterface(node.signature), null/* todo exceptions node.exceptions.toArray(new String[0])*/ );
		base.localVariables = node.localVariables.stream().peek(l -> interfaceName(l.desc)).collect(Collectors.toList());
		int offset;
		if((node.access & ACC_STATIC) == 0) { // instance method
			offset = 1;
			base.visitVarInsn(ALOAD, 0); // load `this`
			if(cast)
				base.visitTypeInsn(CHECKCAST, ownerName);
		} else offset = 0;

		Type method = Type.getMethodType(node.desc);
		for (Type type : method.getArgumentTypes()) {
			base.visitVarInsn(type.getOpcode(ILOAD), offset++);
			//if(type.)
		}

		return null;
	}

	protected static String baseName(String name) {
		return name.replace("net/minecraft", "net/f2bb/api")+"Base";
	}

	/**
	 * can rename names and descriptors
	 */
	protected static String interfaceName(String desc) {
		// todo check if class exists in jar

		if((desc.length() - Math.max(desc.lastIndexOf('['), 0)) > 2) {
			int i = Math.max(desc.lastIndexOf('/'), 0) + 1;
			return desc.substring(0, i).replace("net/minecraft", "net/f2bb/api") + 'I' + desc.substring(i);
		}
		return desc;
	}

	/**
	 * remaps signatures and descriptors to interface
	 */
	protected static String remapSigToInterface(String desc) {
		if(desc == null)
			return null;
		SignatureReader reader = new SignatureReader(desc);
		SignatureWriter writer = new SignatureWriter();
		SignatureRemapper remapper = new SignatureRemapper(writer, new Remapper() {
			@Override
			public String mapType(String internalName) {
				return interfaceName(internalName);
			}
		});
		reader.accept(remapper);
		return writer.toString();
	}

	public static void main(String[] args) {

	}
}
