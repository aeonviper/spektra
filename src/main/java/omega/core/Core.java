package omega.core;

import com.google.inject.Injector;

public class Core {

	private static Injector injector = null;

	public static Injector getInjector() {
		return injector;
	}

	public static void setInjector(Injector injector) {
		Core.injector = injector;
	}

}
