package omega.persistence;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

import java.net.URL;
import java.util.Properties;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.AbstractModule;
import com.zaxxer.hikari.HikariDataSource;

import omega.annotation.Transactional;

public class PersistenceModule extends AbstractModule {

	public PersistenceModule() {
	}

	@Override
	protected final void configure() {

		HikariDataSource ds = new HikariDataSource();

		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource("application.configuration");
			if (url != null) {
				Properties properties = new Properties();
				properties.load(url.openStream());
				ds.setDriverClassName("org.postgresql.Driver");
				ds.setJdbcUrl(properties.getProperty("jdbc.url"));
				ds.setUsername(properties.getProperty("database.username"));
				ds.setPassword(properties.getProperty("database.password"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ds.setJdbcUrl("jdbc:postgresql://localhost:5432/equiti");
		// ds.setUsername("equitiuser");
		// ds.setPassword("normal");

		bind(PersistenceService.class).toInstance(new PersistenceService(ds));
		MethodInterceptor transactionInterceptor = new TransactionInterceptor();
		requestInjection(transactionInterceptor);

		bindInterceptor(any(), annotatedWith(Transactional.class), transactionInterceptor);

	}

}
