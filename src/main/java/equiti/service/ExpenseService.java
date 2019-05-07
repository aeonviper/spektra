package equiti.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import equiti.core.Utility;
import equiti.model.Expense;
import equiti.utility.Json;
import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.service.Decorator;

public class ExpenseService extends BaseService {

	private static final Map<Class, Class> classMap = new HashMap<>() {
		{
			put(Timestamp.class, LocalDateTime.class);
		}
	};

	public static final Decorator<Expense> toDecorator = new Decorator<Expense>() {
		public Expense decorate(Expense entity) {
			if (entity.getDataMap() != null) {
				entity.setData(Json.toJson(entity.getDataMap()));
			}
			return entity;
		}
	};

	public static final Decorator<Expense> fromDecorator = new Decorator<Expense>() {
		public Expense decorate(Expense entity) {
			if (Utility.isNotBlank(entity.getData())) {
				entity.setDataMap((Map<String, Object>) Json.fromJson(entity.getData(), Map.class));
			}
			return entity;
		}
	};

	@Transactional(type = TransactionType.READWRITE)
	public int save(Expense expense) {
		toDecorator.decorate(expense);
		if (expense.getId() != null) {
			return update(expense);
		} else {
			return insert(expense);
		}
	}

	@Transactional(type = TransactionType.READWRITE)
	public int insert(Expense expense) {
		expense.setId(sequence("entitySequence"));
		return write( //
		"insert into expense (id, companyId, name, data, accountId, vendor, type, incurred, categoryName, amount, information) values (?, ?, ?, to_json(?::json), ?, ?, ?, ?, ?, ?, ?)", //
		expense.getId(), expense.getCompanyId(), expense.getName(), expense.getData(), //
		expense.getAccountId(), expense.getVendor(), expense.getType(), expense.getIncurred(), expense.getCategoryName(), //
		expense.getAmount(), expense.getInformation() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int update(Expense expense) {
		return write( //
		"update expense set name = ?, data = to_json(?::json), accountId = ?, vendor = ?, type = ?, incurred = ?, categoryName = ?, amount = ?, information = ? where id = ? and companyId = ?", //
		expense.getName(), expense.getData(), //
		expense.getAccountId(), expense.getVendor(), expense.getType(), expense.getIncurred(), expense.getCategoryName(), //
		expense.getAmount(), expense.getInformation(), //
		expense.getId(), expense.getCompanyId() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int delete(Long id, Long companyId) {
		return write( //
		"delete from expense where id = ? and companyId = ?", //
		id, companyId //
		);
	}

	@Transactional(type = TransactionType.READONLY)
	public Expense find(Long id, Long companyId) {
		return fromDecorator.decorate(find(Expense.class, classMap, Collections.emptyMap(), "select * from expense where id = ? and companyId = ?", id, companyId));
	}

	@Transactional(type = TransactionType.READONLY)
	public List<Expense> list(Long companyId) {
		return fromDecorator.decorate(list(Expense.class, classMap, Collections.emptyMap(), "select * from expense where companyId = ? order by id", companyId));
	}
}
