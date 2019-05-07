package equiti.server;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PathPattern {

	private static Map<String, Pattern> cache = new HashMap<>();

	public static Pattern pattern(String value) {
		Pattern pattern = cache.get(value);
		if (pattern != null) {
			return pattern;
		}
		synchronized (cache) {
			cache.put(value, pattern = Pattern.compile(value));
		}
		return pattern;
	}

}
