package equiti.utility;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class Json {

	private static final Map<Class, ObjectWriter> writerCache = new HashMap<>();
	private static final Map<Class, ObjectReader> readerCache = new HashMap<>();

	public static final ObjectMapper jsonMapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	public static final ObjectReader jsonReader = jsonMapper.readerFor(ObjectNode.class);
	public static final ObjectWriter jsonWriter = jsonMapper.writerFor(ObjectNode.class);

	public static String toJson(Object object) {
		try {
			ObjectWriter writer = writerCache.get(object.getClass());
			if (writer == null) {
				synchronized (writerCache) {
					writerCache.put(object.getClass(), writer = jsonMapper.writerFor(object.getClass()));
				}
			}
			return writer.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T fromJson(String text, Class<T> clazz) {
		try {
			ObjectReader reader = readerCache.get(clazz);
			if (reader == null) {
				synchronized (readerCache) {
					readerCache.put(clazz, reader = jsonMapper.readerFor(clazz));
				}
			}
			return reader.readValue(text);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode readTree(String text) {
		try {
			return jsonReader.readTree(text);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T treeToValue(JsonNode node, Class<T> clazz) {
		try {
			return jsonReader.treeToValue(node, clazz);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String writeValueAsString(Object object) {
		try {
			return jsonMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
