package omega.persistence;

import java.sql.Connection;

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;

import omega.annotation.ExecutionType;
import omega.annotation.TransactionIsolation;
import omega.annotation.TransactionType;
import omega.annotation.Transactional;

public class TransactionInterceptor implements MethodInterceptor {

	@Inject
	PersistenceService persistenceService;

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Transactional transactional = methodInvocation.getMethod().getAnnotation(Transactional.class);
		if (transactional != null) {

			DataSource dataSource = null;
			TransactionType transactionType = null;

			if (persistenceService.isDual()) {
				if (persistenceService.get() == null) {

					if (TransactionTypeService.isSet()) {
						if (TransactionTypeService.isReadOnly()) {
							dataSource = persistenceService.getDataSourceReadOnly();
							transactionType = TransactionType.READONLY;
							// Logger.getLogger().info("Txn override: readOnly");
						} else {
							dataSource = persistenceService.getDataSourceReadWrite();
							transactionType = TransactionType.READWRITE;
							// Logger.getLogger().info("Txn override: readWrite");
						}
					} else {
						if (transactional.type() == TransactionType.READONLY) {
							dataSource = persistenceService.getDataSourceReadOnly();
							transactionType = TransactionType.READONLY;
							// Logger.getLogger().info("Txn: readOnly");
						} else {
							dataSource = persistenceService.getDataSourceReadWrite();
							transactionType = TransactionType.READWRITE;
							// Logger.getLogger().info("Txn: readWrite");
						}
					}

				} else {

					if (persistenceService.get().getTransactionType() == TransactionType.READONLY) {
						// must always use if, not if-else
						if (TransactionType.READWRITE == transactional.type()) {
							String message = "Transaction Type mismatch ! Current Transaction is " + persistenceService.get().getTransactionType() + " but method " + methodInvocation.getClass().getCanonicalName() + "." + methodInvocation.getMethod().getName() + " has Transactional Type " + transactional.type();
							// Logger.getLogger().error(message);
							throw new RuntimeException(this.getClass().getCanonicalName() + " - " + message);
						}
						// this is probably not going to happen, since override can only happen at the beginning not during a thread execution
						// transaction override is only honored by the framework only at the beginning of thread execution. if it wanted read write it should have specified read write in the beginning thus persistService would hold a read write entity manager
						// in order for this to happen it means ThreadLocal of transaction type at beginning of thread execution is one value (read only) and got changed to a different value (read write) in the middle of thread execution, now the framework is re-checking and finding a different value
						if (TransactionTypeService.isSet() && TransactionTypeService.isReadWrite()) {
							String message = "Transaction Type mismatch ! Current Transaction is " + persistenceService.get().getTransactionType() + " but transaction type override is Read Write";
							// Logger.getLogger().error(message);
							throw new RuntimeException(this.getClass().getCanonicalName() + " - " + message);
						}
					}

				}

			} else if (persistenceService.isSingle()) {

				if (persistenceService.get() == null) {
					dataSource = persistenceService.getDataSourceReadWrite();
					transactionType = TransactionType.READWRITE;
					// Logger.getLogger().info("Txn: readWrite");
				}

			}

			Connection connection = null;
			if (persistenceService.get() == null) {

				connection = dataSource.getConnection();
				connection.setAutoCommit(false);

				ExecutionType transactionalExecutionType = transactional.executionType();
				if (transactionalExecutionType == ExecutionType.DEFAULT) {

				} else if (transactionalExecutionType == ExecutionType.BATCH) {
					// unimplemented
				}

				TransactionIsolation transactionalIsolation = transactional.isolation();
				if (transactionalIsolation != TransactionIsolation.DEFAULT) {
					switch (transactionalIsolation) {
					case READ_COMMITTED:
						connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
						break;
					case READ_UNCOMMITTED:
						connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
						break;
					case REPEATABLE_READ:
						connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
						break;
					case SERIALIZABLE:
						connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
						break;
					default:
						break;
					}
				}

				persistenceService.set(new PersistenceTransaction(connection, transactionType));

			} else {

				// check requested transaction isolation
				// Connection databaseConnection = persistenceService.get().getConnection();
				

			}

			// System.out.println(this.getClass().getSimpleName() + "> sqlSession:" + sqlSession);

			Object result = null;

			if (dataSource != null) {

				// if sqlSessionFactory != null that means this is the beginning of the transaction

				try {
					result = methodInvocation.proceed();
					connection.commit();
				} catch (Exception e) {
					if (commitWhenException(transactional, e)) {
						connection.commit();
					} else {
						connection.rollback();
					}

					System.err.println(e);
					throw e;
				} finally {
					connection.close();
					persistenceService.remove();
				}

			} else {

				// an active transaction is already is progress, since persistenceService.get() != null, thus sqlSessionFactory is null

				result = methodInvocation.proceed();

			}

			return result;

		} else {
			return methodInvocation.proceed();
		}
	}

	private boolean commitWhenException(Transactional transactional, Exception e) {
		boolean commit = true;

		for (Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {
			if (rollBackOn.isInstance(e)) {
				commit = false;

				for (Class<? extends Exception> exceptOn : transactional.ignore()) {
					if (exceptOn.isInstance(e)) {
						commit = true;
						break;
					}
				}

				break;
			}
		}

		return commit;
	}
}
