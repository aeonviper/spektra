package equiti.controller;

import com.google.inject.Inject;

import equiti.model.Account;
import equiti.service.AccountService;
import omega.service.TransactionService;

public class AccountController extends ModelController<Account> {

	@Inject
	protected TransactionService transactionService;

	@Inject
	protected AccountService accountService;

	public Account find(Long entityId, Long companyId) {
		return accountService.find(entityId, companyId);
	}

	public Account add(Account entity) {
		accountService.save(entity);
		return entity;
	}

	public Account update(Account entity) {
		Account account = accountService.find(entity.getId(), entity.getCompanyId());
		if (account != null) {
			account.setName(entity.getName());
			account.setType(entity.getType());
			account.setCode(entity.getCode());
			accountService.save(account);
		}
		return account;
	}

	public Account delete(Long entityId, Long companyId) {
		accountService.delete(entityId, companyId);
		return null;
	}

}
