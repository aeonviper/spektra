package equiti.controller;

import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;

public class BaseController {

	public final static HttpMethod GET = HttpMethods.GET;
	public final static HttpMethod POST = HttpMethods.POST;
	public final static HttpMethod PUT = HttpMethods.PUT;
	public final static HttpMethod DELETE = HttpMethods.DELETE;

	public final static Route badRequest = Directives.complete(StatusCodes.BAD_REQUEST, "Bad request");
	public final static Route notFound = Directives.complete(StatusCodes.NOT_FOUND, "Not found");
	public final static Route genericError = Directives.complete(StatusCodes.INTERNAL_SERVER_ERROR, "My bad");
	public final static Route ok = Directives.complete(StatusCodes.OK);

}
