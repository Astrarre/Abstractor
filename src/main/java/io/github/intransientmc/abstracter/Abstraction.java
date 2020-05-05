package io.github.intransientmc.abstracter;

import com.google.gson.Gson;
import io.github.intransientmc.abstracter.emitter.BaseEmitter;
import io.github.intransientmc.abstracter.emitter.InterfaceEmitter;
import io.github.intransientmc.abstracter.schema.Cls;
import io.github.intransientmc.abstracter.schema.Field;
import io.github.intransientmc.abstracter.schema.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

public class Abstraction implements Opcodes {
	private final static class Json {
		public Cls[] classes;
		private String information;
	}

	public static final String EXPOSE_TYPE = "io/github/intransientmc/jarstripper/annotations/Strip";

	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Json json = gson.fromJson(reader, Json.class);
		reader.close();
		System.out.println("Information: " + json.information);
		Cls[] classes = json.classes;
		for (Cls cls : classes) {

		}
	}

	private static int absInterfaceMethod(ClassVisitor visitor, Collection<Method> methods) {
		int count = 0;
		for (Method method : methods) {
			if (method.is_invoker_exposed) {
				count++;
				InterfaceEmitter.emitInvoker(visitor, method.yarnName, abstractMethodDesc('('+String.join("", Arrays.asList(method.yarnParameterTypes))+')'+method.yarnReturnType), Modifier.isStatic(method.access));
			}
		}
		return count;
	}

	private static int absInterfaceField(ClassVisitor visitor, Collection<Field> fields) {
		int count = 0;
		for (Field field : fields) {
			if(field.is_getter_exposed) {
				count++;
				InterfaceEmitter.emitGetter(visitor, field.yarnName, field.yarnType, Modifier.isStatic(field.access));
			}
			if(field.is_setter_exposed) {
				count++;
				InterfaceEmitter.emitSetter(visitor, field.yarnName, field.yarnType, Modifier.isStatic(field.access));
			}
		}
		return count;
	}

	private static int absBaseMethods(Cls cls, ClassVisitor visitor, Collection<Method> methods) {
		int count = 0;
		for (Method method : methods) {
			if (method.is_invoker_implemented) {
				count++;
				String desc = '(' + String.join("", Arrays.asList(method.yarnParameterTypes)) + ')' + method.yarnReturnType;
				BaseEmitter.emitAbstractedMethod(visitor, method.yarnOwner, method.yarnName, abstractMethodDesc(desc), desc, Modifier.isStatic(method.access), Modifier.isFinal(method.access), method.is_invoker_exposed, Modifier.isInterface(cls.access));
			}
		}
		return count;
	}

	private static int absBaseFields(ClassVisitor visitor, Collection<Field> fields) {
		int count = 0;
		for (Field field : fields) {
			if(field.is_getter_exposed) {
				count++;
				InterfaceEmitter.emitGetter(visitor, field.yarnName, field.yarnType, Modifier.isStatic(field.access));
			}
			if(field.is_setter_exposed) {
				count++;
				InterfaceEmitter.emitSetter(visitor, field.yarnName, field.yarnType, Modifier.isStatic(field.access));
			}
		}
		return count;
	}


	// todo allow custom handling for names
	public static String interfaceName(String name) {
		int last = Math.max(0, name.lastIndexOf('/')) + 1;
		return name.substring(0, last) + 'I' + name.substring(last);
	}

	public static String baseName(String name) {
		return name + "Base";
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