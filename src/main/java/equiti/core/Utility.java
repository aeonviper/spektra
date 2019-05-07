package equiti.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Utility {

	public static final DateTimeFormatter fileDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");
	public static final DateTimeFormatter logDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter fullDateTimeFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss");
	public static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
	public static final DateTimeFormatter isoDateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;

	public static final String separator = ":";

	public static Properties properties = new Properties();

	static {
		URL url = null;
		try {
			url = Thread.currentThread().getContextClassLoader().getResource("application.properties");
			if (url != null) {
				properties.load(url.openStream());

				File file = new File(url.getFile());
				File baseDirectory = file.getParentFile();
				File rootDirectory = new File(baseDirectory.getParentFile().getParentFile(), ".");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String stripText(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		return str.trim();
	}

	public static LocalDateTime now() {
		return LocalDateTime.now();
	}

	public static boolean same(String text1, String text2) {
		if (text1 == null && text2 == null) {
			return true;
		} else if (text1 != null && text2 != null) {
			return text1.equals(text2);
		}
		return false;
	}

	public static <K, T> Map<K, T> mapify(List<T> list, Class<K> clazz, String methodName, boolean useLast) {
		if (list == null) {
			throw new RuntimeException("List is null");
		} else {
			Map<K, T> map = new HashMap<K, T>();
			if (!list.isEmpty()) {
				T entity = list.get(0);
				Method getKey;
				try {
					getKey = entity.getClass().getDeclaredMethod(methodName);
					if (getKey != null) {
						for (T element : list) {
							if (useLast || !map.containsKey((K) getKey.invoke(element))) {
								map.put((K) getKey.invoke(element), element);
							}
						}
					}
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return map;
		}
	}

	public static <K, T> Map<K, T> mapify(List<T> list, Class<K> clazz, String methodName) {
		return mapify(list, clazz, methodName, true);
	}

	public static <K, T> Map<Long, T> mapify(List<T> list) {
		return mapify(list, Long.class, "getId", true);
	}

	public static <T> T coalesce(T value, T defaultValue) {
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public static String stringify(Object object) {
		return object == null ? "" : String.valueOf(object);
	}

	public static boolean isTrue(Boolean expression) {
		return Boolean.TRUE.equals(expression);
	}

	public static boolean isNotBlank(String text) {
		return text != null && !text.trim().isEmpty();
	}

	public static String escapeSQLLike(String escape, String sql) {
		return sql.replace("%", escape + "%").replace("_", escape + "_");
	}

	public static String findSQLLikeEscape(String s) {
		for (String candidate : new String[] { "!", "^", "#", "|", "~", "$", "-", "_", "?" }) {
			if (!s.contains(candidate)) {
				return candidate;
			}
		}
		return null;
	}

	public static String csvField(Object object) {
		if (object == null) {
			return "";
		} else {
			String text = String.valueOf(object);
			return "\"" + text.replace("\"", "\"\"") + "\"";
		}
	}

}
