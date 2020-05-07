import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class a {
	public static void main(String[] args) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		JsonObject object = new JsonObject();
		object.addProperty("a=a", "a");
		System.out.println(gson.toJson(object));
	}
}
