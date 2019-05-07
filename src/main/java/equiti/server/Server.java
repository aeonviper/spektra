package equiti.server;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.HttpCookie;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import equiti.controller.AccountController;
import equiti.controller.BaseController;
import equiti.controller.CompanyController;
import equiti.controller.ContactController;
import equiti.controller.ExpenseController;
import equiti.controller.ProductCategoryController;
import equiti.controller.ProductController;
import equiti.core.Container;
import omega.core.Core;
import equiti.model.Account;
import equiti.model.Contact;
import equiti.model.Expense;
import equiti.model.Product;
import equiti.model.ProductCategory;

public class Server extends AllDirectives {

	public static final String serverHost = "localhost";
	public static final Integer serverPort = 8081;

	public static void main(String[] args) {
		final Injector injector = Guice.createInjector(new AbstractModule() {
			protected void configure() {
				install(new omega.persistence.PersistenceModule());
			}
		});
		Core.setInjector(injector);

		final ActorSystem system = ActorSystem.create();
		final Materializer materializer = ActorMaterializer.create(system);
		final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = new Server().createRoute(injector).flow(system, materializer);
		final CompletionStage<ServerBinding> serverBindingFuture = Http.get(system).bindAndHandle(routeFlow.async("my-blocking-dispatcher"), ConnectHttp.toHost(serverHost, serverPort), materializer);

		System.out.println("Server started");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		serverBindingFuture.thenCompose(ServerBinding::unbind).thenAccept(unbound -> {
			system.terminate();
			System.out.println("Server stopped");
		});

	}

	public Route createRoute(Injector injector) {
		// Route helloRoute = parameterOptional("name", optName -> {
		// String name = optName.orElse("Mister X");
		// return complete("Hello " + name + "!");
		// });

		final String jwtIssuer = "equiti.id";
		final String secret = "secret";
		final Container<JWTVerifier> jwtVerifier = new Container<>();
		final Container<String> token = new Container<>();
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			token.set(JWT.create().withIssuer(jwtIssuer).withClaim("companyId", 15).sign(algorithm));
			jwtVerifier.set(JWT.require(algorithm).withIssuer(jwtIssuer).build());
		} catch (JWTCreationException e) {
			e.printStackTrace();
		}

		return optionalHeaderValueByName("Authorization", bearerToken -> {
			Container<Long> companyId = new Container<>();
			try {
				if (bearerToken.isPresent()) {
					String tokenValue = bearerToken.get().substring("Bearer ".length());
					DecodedJWT decodedJwt = jwtVerifier.get().verify(tokenValue);
					Claim companyIdClaim = decodedJwt.getClaim("companyId");
					if (!companyIdClaim.isNull()) {
						companyId.set(companyIdClaim.asLong());
					}
				}
			} catch (JWTVerificationException e) {
				e.printStackTrace();
			}
			return concat( //
			pathPrefix("account", () -> extractRequest(request -> entity(Unmarshaller.entityToString(), entity -> injector.getInstance(AccountController.class).handle("account", Account.class, injector, request, entity, companyId.get())))), //
			pathPrefix("contact", () -> extractRequest(request -> entity(Unmarshaller.entityToString(), entity -> injector.getInstance(ContactController.class).handle("contact", Contact.class, injector, request, entity, companyId.get())))), //
			pathPrefix("expense", () -> extractRequest(request -> entity(Unmarshaller.entityToString(), entity -> injector.getInstance(ExpenseController.class).handle("expense", Expense.class, injector, request, entity, companyId.get())))), //
			pathPrefix("productCategory", () -> extractRequest(request -> entity(Unmarshaller.entityToString(), entity -> injector.getInstance(ProductCategoryController.class).handle("productCategory", ProductCategory.class, injector, request, entity, companyId.get())))), //
			pathPrefix("product", () -> extractRequest(request -> entity(Unmarshaller.entityToString(), entity -> injector.getInstance(ProductController.class).handle("product", Product.class, injector, request, entity, companyId.get())))), //
			pathPrefix("company", () -> extractRequest(request -> entity(Unmarshaller.entityToString(), entity -> injector.getInstance(CompanyController.class).handle("company", injector, request, entity)))), //
			path("jwt", () -> setCookie(HttpCookie.create("jwt", token.get()), () -> BaseController.ok)), //
			path("", () -> BaseController.ok), //
			pass(() -> BaseController.notFound) //
			);
		});

		// return optionalCookie("jwt", jwtCookie -> {
		// Container<Long> companyId = new Container<>();
		// try {
		// if (jwtCookie.isPresent()) {
		// DecodedJWT decodedJwt = jwtVerifier.get().verify(jwtCookie.get().value());
		// Claim companyIdClaim = decodedJwt.getClaim("companyId");
		// if (!companyIdClaim.isNull()) {
		// companyId.set(companyIdClaim.asLong());
		// }
		// }
		// } catch (JWTVerificationException e) {
		// e.printStackTrace();
		// }
		// return concat( //
		//
		// );
		// });

	}

}
