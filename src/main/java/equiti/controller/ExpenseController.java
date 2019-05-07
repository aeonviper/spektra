package equiti.controller;

import com.google.inject.Inject;

import equiti.model.Expense;
import equiti.service.ExpenseService;
import omega.service.TransactionService;

public class ExpenseController extends ModelController<Expense> {

	@Inject
	protected TransactionService transactionService;

	@Inject
	protected ExpenseService expenseService;

	public Expense find(Long entityId, Long companyId) {
		return expenseService.find(entityId, companyId);
	}

	public Expense add(Expense entity) {
		expenseService.save(entity);
		return entity;
	}

	public Expense update(Expense entity) {
		Expense expense = expenseService.find(entity.getId(), entity.getCompanyId());
		if (expense != null) {
			expense.setName(entity.getName());
			expense.setAccountId(entity.getAccountId());
			expense.setVendor(entity.getVendor());
			expense.setType(entity.getType());
			expense.setIncurred(entity.getIncurred());
			expense.setCategoryName(entity.getCategoryName());
			expense.setAmount(entity.getAmount());
			expense.setInformation(entity.getInformation());
			expenseService.save(expense);
		}
		return expense;
	}

	public Expense delete(Long entityId, Long companyId) {
		expenseService.delete(entityId, companyId);
		return null;
	}
}
