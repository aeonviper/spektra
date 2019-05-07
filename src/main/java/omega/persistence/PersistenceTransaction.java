package omega.persistence;

import java.sql.Connection;

import javax.sql.DataSource;

import omega.annotation.TransactionType;

public class PersistenceTransaction {

	private Connection connection;
	private TransactionType transactionType;

	public PersistenceTransaction(Connection connection, TransactionType transactionType) {
		this.connection = connection;
		this.transactionType = transactionType;
	}

	public Connection getConnection() {
		return connection;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

}
