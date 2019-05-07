package equiti.service;

import java.util.List;
import java.util.Map;

import equiti.core.Utility;
import equiti.model.Company;
import equiti.utility.Json;
import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.service.Decorator;

public class CompanyService extends BaseService {

	public static final Decorator<Company> toDecorator = new Decorator<Company>() {
		public Company decorate(Company entity) {
			if (entity.getDataMap() != null) {
				entity.setData(Json.toJson(entity.getDataMap()));
			}
			return entity;
		}
	};

	public static final Decorator<Company> fromDecorator = new Decorator<Company>() {
		public Company decorate(Company entity) {
			if (Utility.isNotBlank(entity.getData())) {
				entity.setDataMap((Map<String, Object>) Json.fromJson(entity.getData(), Map.class));
			}
			return entity;
		}
	};

	@Transactional(type = TransactionType.READWRITE)
	public int save(Company company) {
		toDecorator.decorate(company);
		if (company.getId() != null) {
			return update(company);
		} else {
			return insert(company);
		}
	}

	@Transactional(type = TransactionType.READWRITE)
	public int insert(Company company) {
		company.setId(sequence("entitySequence"));
		return write( //
		"insert into company (id, name, data, contactName, contactEmail, contactPhone, address, city, state, country) values (?, ?, to_json(?::json), ?, ?, ?, ?, ?, ?, ?)", //
		company.getId(), company.getName(), company.getData(), //
		company.getContactName(), company.getContactEmail(), company.getContactPhone(), company.getAddress(), company.getCity(), //
		company.getState(), company.getCountry() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int update(Company company) {
		return write( //
		"update company set name = ?, data = to_json(?::json), contactName = ?, contactEmail = ?, contactPhone = ?, address = ?, city = ?, state = ?, country = ? where id = ?", //
		company.getName(), company.getData(), //
		company.getContactName(), company.getContactEmail(), company.getContactPhone(), company.getAddress(), company.getCity(), //
		company.getState(), company.getCountry(), //
		company.getId() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int delete(Long id) {
		return write( //
		"delete from company where id = ?", //
		id //
		);
	}

	@Transactional(type = TransactionType.READONLY)
	public Company find(Long id) {
		return fromDecorator.decorate(find(Company.class, "select * from company where id = ?", id));
	}

	@Transactional(type = TransactionType.READONLY)
	public List<Company> list() {
		return fromDecorator.decorate(list(Company.class, "select * from company order by id"));
	}
}
