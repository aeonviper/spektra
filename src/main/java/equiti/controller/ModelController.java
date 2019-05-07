package equiti.controller;

import static akka.http.javadsl.server.Directives.complete;

import java.util.regex.Matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.Route;
import equiti.model.Model;
import equiti.server.PathPattern;
import equiti.utility.Json;

public abstract class ModelController<T extends Model> extends BaseController {

	public Route handle(String type, Class<T> clazz, Injector injector, HttpRequest request, String body, Long companyId) {
		String path = request.getUri().path();
		Matcher matcher;

		if (request.method() != PUT && (matcher = PathPattern.pattern("/" + type + "/([0-9]+)").matcher(path)).matches()) {
			Long entityId = Long.parseLong(matcher.group(1));
			if (request.method() == GET) {
				return complete(Json.writeValueAsString(find(entityId, companyId)));
			} else if (request.method() == DELETE) {
				delete(entityId, companyId);
				return ok;
			}
		} else {
			JsonNode rootNode = Json.readTree(body);
			T entity = Json.treeToValue(rootNode.get(type), clazz);
			entity.setCompanyId(companyId);
			if (request.method() == POST && (matcher = PathPattern.pattern("/" + type).matcher(path)).matches()) {
				return complete(Json.writeValueAsString(add(entity)));
			} else if (request.method() == PUT && (matcher = PathPattern.pattern("/" + type + "/([0-9]+)").matcher(path)).matches()) {
				Long id = Long.parseLong(matcher.group(1));
				entity.setId(id);
				return complete(Json.writeValueAsString(update(entity)));
			}
		}

		return genericError;
	}

	public abstract T find(Long entityId, Long companyId);

	public abstract T add(T entity);

	public abstract T update(T updatedEntity);

	public abstract T delete(Long entityId, Long companyId);

}
