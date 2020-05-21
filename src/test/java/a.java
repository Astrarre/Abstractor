import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class a {
	private static final List<String> bruh = null;
	public static void main(String[] args) throws IOException {
		InputStream stream = a.class.getResourceAsStream("/"+a.class.getName().replace('.', '/')+".class");
		ClassReader reader = new ClassReader(stream);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for (MethodNode method : node.methods) {
			System.out.println(method.signature);
		}

		for (FieldNode field : node.fields) {
			System.out.println(field.signature);
		}
	}

	private static List<String> test() {
		return bruh;
	}
}
