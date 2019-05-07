package equiti.controller;

import static akka.http.javadsl.server.Directives.complete;

import java.util.regex.Matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Injector;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.Route;
import equiti.model.Company;
import equiti.server.PathPattern;
import equiti.service.CompanyService;
import equiti.utility.Json;
import omega.service.TransactionService;

public class CompanyController extends BaseController {

	@Inject
	protected TransactionService transactionService;

	@Inject
	protected CompanyService companyService;

	public Route handle(String type, Injector injector, HttpRequest request, String body) {
		String path = request.getUri().path();
		Matcher matcher;

		if (request.method() != PUT && (matcher = PathPattern.pattern("/" + type + "/([0-9]+)").matcher(path)).matches()) {
			Long entityId = Long.parseLong(matcher.group(1));
			if (request.method() == GET) {
				return complete(Json.writeValueAsString(find(entityId)));
			} else if (request.method() == DELETE) {
				delete(entityId);
				return ok;
			}
		} else {
			JsonNode rootNode = Json.readTree(body);
			Company entity = Json.treeToValue(rootNode.get(type), Company.class);
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

	public Company find(Long entityId) {
		return companyService.find(entityId);
	}

	public Company add(Company entity) {
		companyService.save(entity);
		return entity;
	}

	public Company update(Company entity) {
		Company company = companyService.find(entity.getId());
		if (company != null) {
			company.setName(entity.getName());
			company.setContactName(entity.getContactName());
			company.setContactEmail(entity.getContactEmail());
			company.setContactPhone(entity.getContactPhone());
			company.setAddress(entity.getAddress());
			company.setCity(entity.getCity());
			company.setState(entity.getState());
			company.setCountry(entity.getCountry());
			companyService.save(company);
		}
		return company;
	}

	public Company delete(Long entityId) {
		companyService.delete(entityId);
		return null;
	}
}
