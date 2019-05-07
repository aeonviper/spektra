package equiti.service;

import java.util.List;
import java.util.Map;

import equiti.core.Utility;
import equiti.model.Contact;
import equiti.utility.Json;
import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.service.Decorator;

public class ContactService extends BaseService {

	public static final Decorator<Contact> toDecorator = new Decorator<Contact>() {
		public Contact decorate(Contact entity) {
			if (entity.getDataMap() != null) {
				entity.setData(Json.toJson(entity.getDataMap()));
			}
			return entity;
		}
	};

	public static final Decorator<Contact> fromDecorator = new Decorator<Contact>() {
		public Contact decorate(Contact entity) {
			if (Utility.isNotBlank(entity.getData())) {
				entity.setDataMap((Map<String, Object>) Json.fromJson(entity.getData(), Map.class));
			}
			return entity;
		}
	};

	@Transactional(type = TransactionType.READWRITE)
	public int save(Contact contact) {
		toDecorator.decorate(contact);
		if (contact.getId() != null) {
			return update(contact);
		} else {
			return insert(contact);
		}
	}

	@Transactional(type = TransactionType.READWRITE)
	public int insert(Contact contact) {
		contact.setId(sequence("entitySequence"));
		return write( //
		"insert into contact (id, companyId, name, data, type, code, email, phone, information, address, city, state, country) values (?, ?, ?, to_json(?::json), ?, ?, ?, ?, ?, ?, ?, ?, ?)", //
		contact.getId(), contact.getCompanyId(), contact.getName(), contact.getData(), //
		contact.getType(), contact.getCode(), contact.getEmail(), contact.getPhone(), contact.getInformation(), //
		contact.getAddress(), contact.getCity(), contact.getState(), contact.getCountry() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int update(Contact contact) {
		return write( //
		"update contact set name = ?, data = to_json(?::json), type = ?, code = ?, email = ?, phone = ?, information = ?, address = ?, city = ?, state = ?, country = ? where id = ? and companyId = ?", //
		contact.getName(), contact.getData(), //
		contact.getType(), contact.getCode(), contact.getEmail(), contact.getPhone(), contact.getInformation(), //
		contact.getAddress(), contact.getCity(), contact.getState(), contact.getCountry(), //
		contact.getId(), contact.getCompanyId() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int delete(Long id, Long companyId) {
		return write( //
		"delete from contact where id = ? and companyId = ?", //
		id, companyId //
		);
	}

	@Transactional(type = TransactionType.READONLY)
	public Contact find(Long id, Long companyId) {
		return fromDecorator.decorate(find(Contact.class, "select * from contact where id = ? and companyId = ?", id, companyId));
	}

	@Transactional(type = TransactionType.READONLY)
	public List<Contact> list(Long companyId) {
		return fromDecorator.decorate(list(Contact.class, "select * from contact where companyId = ? order by id", companyId));
	}
}
