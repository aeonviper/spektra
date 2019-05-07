package omega.service;

import com.google.inject.Singleton;

import omega.annotation.TransactionType;
import omega.annotation.Transactional;

@Singleton
public class TransactionService {

	@Transactional(type = TransactionType.READWRITE)
	public void executeReadWrite(TransactionContext transactionContext) {
		transactionContext.execute();
	}

	@Transactional(type = TransactionType.READONLY)
	public void executeReadOnly(TransactionContext transactionContext) {
		transactionContext.execute();
	}

	@Transactional(type = TransactionType.READWRITE)
	public void executeReadWrite(TransactionContext transactionContext, Object... array) {
		transactionContext.execute(array);
	}

	@Transactional(type = TransactionType.READONLY)
	public void executeReadOnly(TransactionContext transactionContext, Object... array) {
		transactionContext.execute(array);
	}

	@Transactional(type = TransactionType.READWRITE)
	public <T> T actionReadWrite(TransactionContext<T> transactionContext) {
		return transactionContext.action();
	}

	@Transactional(type = TransactionType.READONLY)
	public <T> T actionReadOnly(TransactionContext<T> transactionContext) {
		return transactionContext.action();
	}

	@Transactional(type = TransactionType.READWRITE)
	public <T> T actionReadWrite(TransactionContext<T> transactionContext, Object... array) {
		return transactionContext.action(array);
	}

	@Transactional(type = TransactionType.READONLY)
	public <T> T actionReadOnly(TransactionContext<T> transactionContext, Object... array) {
		return transactionContext.action(array);
	}

}
