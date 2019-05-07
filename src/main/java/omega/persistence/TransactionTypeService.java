package omega.persistence;

import omega.annotation.TransactionType;

public class TransactionTypeService {

	private final static ThreadLocal<TransactionType> typeHolder = new ThreadLocal<TransactionType>();

	public static void setReadOnly() {
		typeHolder.set(TransactionType.READONLY);
	}

	public static void setReadWrite() {
		typeHolder.set(TransactionType.READWRITE);
	}

	public static boolean isReadOnly() {
		return typeHolder.get() == TransactionType.READONLY;
	}

	public static boolean isReadWrite() {
		return typeHolder.get() == TransactionType.READWRITE;
	}

	public static boolean isSet() {
		return typeHolder.get() != null;
	}

	public static void remove() {
		typeHolder.remove();
	}

}
