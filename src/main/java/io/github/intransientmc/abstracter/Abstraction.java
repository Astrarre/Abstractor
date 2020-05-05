package io.github.intransientmc.abstracter;

import com.google.gson.Gson;
import io.github.intransientmc.abstracter.emitter.BaseEmitter;
import io.github.intransientmc.abstracter.emitter.InterfaceEmitter;
import io.github.intransientmc.abstracter.schema.Cls;
import io.github.intransientmc.abstracter.schema.Field;
import io.github.intransientmc.abstracter.schema.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Abstraction implements Opcodes {
	private static final String OBJECT_TYPE = Type.getInternalName(Object.class);
	public static final String EXPOSE_TYPE = "io/github/intransientmc/jarstripper/annotations/Strip";

	public static final Set<String> CLASSES = new HashSet<>();

	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Cls[] classes = gson.fromJson(reader, Cls[].class);
		reader.close();
		for (Cls aClass : classes) {
			CLASSES.add(aClass.yarnName.substring(1, aClass.yarnName.length()-1));
		}
		Map<String, ClassWriter> writers = new HashMap<>();
		for (Cls cls : classes) {
			String name = cls.yarnName.substring(1, cls.yarnName.length()-1);
			ClassWriter interfaceWriter = make(cls, ACC_PUBLIC | ACC_FINAL | ACC_INTERFACE, interfaceName(name), OBJECT_TYPE);
			int written = absInterfaceField(interfaceWriter, cls.fields);
			written += absInterfaceMethod(interfaceWriter, cls.methods);
			if(written > 0)
				writers.put(interfaceName(name), interfaceWriter);

			String supername = OBJECT_TYPE;
			if(!cls.parents.isEmpty())
				supername = cls.parents.stream().findFirst().get();
			ClassWriter baseWriter = make(cls, ACC_PUBLIC | (cls.access & ACC_ABSTRACT), baseName(name), supername);
			int written2 = absBaseFields(baseWriter, cls.fields);
			written2 += absBaseMethods(cls, baseWriter, cls.methods);
			if(written2 > 0)
				writers.put(baseName(name), baseWriter);
		}

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(args[1]));
		for (Map.Entry<String, ClassWriter> entry : writers.entrySet()) {
			String key = entry.getKey();
			out.putNextEntry(new ZipEntry(key+".class"));
			ClassWriter writer = entry.getValue();
			out.write(writer.toByteArray());
			out.closeEntry();
		}
		out.close();
	}

	private static ClassWriter make(Cls cls, int access, String name, String uppser) {
		ClassWriter writer = new ClassWriter(0);
		List<String> list = new ArrayList<>(cls.interfaces);
		for (int i = 0; i < list.size(); i++) {
			list.set(i, interfaceName(list.get(i)));
		}
		writer.visit(V1_8, access, name, null, uppser, list.toArray(new String[0]));
		return writer;
	}

	private static int absInterfaceMethod(ClassVisitor visitor, Collection<Method> methods) {
		int count = 0;
		for (Method method : methods) {
			if (method.invokable_interface) {
				count++;
				InterfaceEmitter.emitInvoker(visitor, method.yarnName, abstractMethodDesc('(' + String.join("", Arrays.asList(method.yarnParameterTypes)) + ')' + method.yarnReturnType), Modifier.isStatic(method.access));
			}
		}
		return count;
	}

	private static int absInterfaceField(ClassVisitor visitor, Collection<Field> fields) {
		int count = 0;
		for (Field field : fields) {
			if (field.getter_interface) {
				count++;
				InterfaceEmitter.emitGetter(visitor, field.yarnName, field.yarnType, Modifier.isStatic(field.access));
			}
			if (field.setter_interface) {
				count++;
				InterfaceEmitter.emitSetter(visitor, field.yarnName, field.yarnType, Modifier.isStatic(field.access));
			}
		}
		return count;
	}

	private static int absBaseMethods(Cls cls, ClassVisitor visitor, Collection<Method> methods) {
		int count = 0;
		for (Method method : methods) {
			if (method.extensible || method.invokable_impl) {
				count++;
				String desc = '(' + String.join("", Arrays.asList(method.yarnParameterTypes)) + ')' + method.yarnReturnType;
				if (method.extensible)
					BaseEmitter.emitExtensible(visitor, method.yarnOwner, baseName(method.yarnName), method.yarnName, abstractMethodDesc(desc), desc, Modifier.isStatic(method.access), method.invokable_interface, Modifier.isInterface(cls.access));
				else BaseEmitter.emitInvoker(visitor, method.yarnOwner, method.yarnName, abstractMethodDesc(desc), desc, Modifier.isStatic(method.access), Modifier.isFinal(method.access), method.invokable_interface, Modifier.isInterface(cls.access));
			}
		}
		return count;
	}

	private static int absBaseFields(ClassVisitor visitor, Collection<Field> fields) {
		int count = 0;
		for (Field field : fields) {
			if (field.getter_impl) {
				count++;
				BaseEmitter.emitGetter(visitor, field.yarnName, field.name, field.yarnType, interfaceName(field.yarnType), Modifier.isStatic(field.access), field.getter_interface);
			}
			if (field.setter_impl) {
				count++;
				BaseEmitter.emitGetter(visitor, field.yarnName, field.name, field.yarnType, interfaceName(field.yarnType), Modifier.isStatic(field.access), field.setter_interface);
			}
		}
		return count;
	}


	// todo allow custom handling for names
	public static String interfaceName(String name) {
		if(CLASSES.contains(name)) {
			int last = Math.max(0, name.lastIndexOf('/')) + 1;
			return name.substring(0, last) + 'I' + name.substring(last);
		}
		return name;
	}

	public static String baseName(String name) {
		if(CLASSES.contains(name)) {
			return name + "Base";
		}
		return name;
	}

	public static String abstractMethodDesc(String strDesc) {
		Type desc = Type.getMethodType(strDesc);
		Type[] types = desc.getArgumentTypes();
		for (int i = 0; i < types.length; i++) {
			types[i] = abstractDesc(types[i]);
		}
		return Type.getMethodDescriptor(abstractDesc(desc.getReturnType()), types);
	}

	public static Type abstractDesc(Type desc) {
		int nests = 0;
		Type val = desc;
		if (desc.getSort() == Type.ARRAY) {
			while (val.getSort() == Type.ARRAY) {
				nests++;
				val = val.getElementType();
			}
		} else if (desc.getSort() != Type.OBJECT) {
			return desc; // primitive
		}
		String newName = 'L' + interfaceName(val.getInternalName()) + ';';
		if (nests > 0) for (int i = 0; i < nests; i++) { newName = "[" + newName; }
		return Type.getType(newName);
	}
}