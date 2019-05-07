package equiti.controller;

import com.google.inject.Inject;

import equiti.model.Contact;
import equiti.service.ContactService;
import omega.service.TransactionService;

public class ContactController extends ModelController<Contact> {

	@Inject
	protected TransactionService transactionService;

	@Inject
	protected ContactService contactService;

	public Contact find(Long entityId, Long companyId) {
		return contactService.find(entityId, companyId);
	}

	public Contact add(Contact entity) {
		contactService.save(entity);
		return entity;
	}

	public Contact update(Contact entity) {
		Contact contact = contactService.find(entity.getId(), entity.getCompanyId());
		if (contact != null) {
			contact.setName(entity.getName());
			contact.setType(entity.getType());
			contact.setCode(entity.getCode());
			contact.setEmail(entity.getEmail());
			contact.setPhone(entity.getPhone());
			contact.setInformation(entity.getInformation());
			contact.setAddress(entity.getAddress());
			contact.setCity(entity.getCity());
			contact.setState(entity.getState());
			contact.setCountry(entity.getCountry());
			contactService.save(contact);
		}
		return contact;
	}

	public Contact delete(Long entityId, Long companyId) {
		contactService.delete(entityId, companyId);
		return null;
	}
}
