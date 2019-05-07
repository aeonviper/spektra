package equiti.service;

import java.util.List;
import java.util.Map;

import equiti.core.Utility;
import equiti.model.Account;
import equiti.utility.Json;
import omega.annotation.TransactionType;
import omega.annotation.Transactional;
import omega.service.Decorator;

public class AccountService extends BaseService {

	public static final Decorator<Account> toDecorator = new Decorator<Account>() {
		public Account decorate(Account entity) {
			if (entity.getDataMap() != null) {
				entity.setData(Json.toJson(entity.getDataMap()));
			}
			return entity;
		}
	};

	public static final Decorator<Account> fromDecorator = new Decorator<Account>() {
		public Account decorate(Account entity) {
			if (Utility.isNotBlank(entity.getData())) {
				entity.setDataMap((Map<String, Object>) Json.fromJson(entity.getData(), Map.class));
			}
			return entity;
		}
	};

	@Transactional(type = TransactionType.READWRITE)
	public int save(Account account) {
		toDecorator.decorate(account);
		if (account.getId() != null) {
			return update(account);
		} else {
			return insert(account);
		}
	}

	@Transactional(type = TransactionType.READWRITE)
	public int insert(Account account) {
		account.setId(sequence("entitySequence"));
		return write( //
		"insert into account (id, companyId, name, data, type, code) values (?, ?, ?, to_json(?::json), ?, ?)", //
		account.getId(), account.getCompanyId(), account.getName(), account.getData(), //
		account.getType(), account.getCode() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int update(Account account) {
		return write( //
		"update account set name = ?, data = to_json(?::json), type = ?, code = ? where id = ? and companyId = ?", //
		account.getName(), account.getData(), //
		account.getType(), account.getCode(), //
		account.getId(), account.getCompanyId() //
		);
	}

	@Transactional(type = TransactionType.READWRITE)
	public int delete(Long id, Long companyId) {
		return write( //
		"delete from account where id = ? and companyId = ?", //
		id, companyId //
		);
	}

	@Transactional(type = TransactionType.READONLY)
	public Account find(Long id, Long companyId) {
		return fromDecorator.decorate(find(Account.class, "select * from account where id = ? and companyId = ?", id, companyId));
	}

	@Transactional(type = TransactionType.READONLY)
	public List<Account> list(Long companyId) {
		return fromDecorator.decorate(list(Account.class, "select * from account where companyId = ? order by id", companyId));
	}
}
