package omega.persistence;

import javax.sql.DataSource;
import com.google.inject.Singleton;

@Singleton
public class PersistenceService {

	private enum Type {
		SINGLE, DUAL;
	}

	private Type type;

	private final ThreadLocal<PersistenceTransaction> persistenceTransactionRepository = new ThreadLocal<PersistenceTransaction>();

	public PersistenceTransaction get() {
		return persistenceTransactionRepository.get();
	}

	public void set(PersistenceTransaction persistenceTransaction) {
		persistenceTransactionRepository.set(persistenceTransaction);
	}

	public void remove() {
		persistenceTransactionRepository.remove();
	}

	DataSource dataSourceReadWrite;
	DataSource dataSourceReadOnly;
	
	public PersistenceService(DataSource dataSourceReadWrite) {
		this.dataSourceReadWrite = dataSourceReadWrite;
		type = Type.SINGLE;
	}
	
	public PersistenceService(DataSource dataSourceReadWrite, DataSource dataSourceReadOnly) {
		this.dataSourceReadWrite = dataSourceReadWrite;
		this.dataSourceReadOnly = dataSourceReadOnly;
		type = Type.DUAL;
	}

	public DataSource getDataSourceReadWrite() {
		return dataSourceReadWrite;
	}

	public DataSource getDataSourceReadOnly() {
		return dataSourceReadWrite;
	}

	public boolean isSingle() {
		return type == Type.SINGLE;
	}

	public boolean isDual() {
		return type == Type.DUAL;
	}

}
