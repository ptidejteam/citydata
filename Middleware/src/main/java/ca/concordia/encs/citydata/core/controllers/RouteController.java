package ca.concordia.encs.citydata.core.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/***
 * This java class is to print all available routes
 *
 * @author Sikandar Ejaz
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/routes")
public class RouteController {

	private final RequestMappingHandlerMapping handlerMapping;

	@Autowired
	public RouteController(ApplicationContext applicationContext) {
		this.handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
	}

	@GetMapping("/list")
	public List<String> listAllRoutes() {
		final List<String> routes = new ArrayList<>();

		handlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
			final String methodInfo = handlerMethod.getMethod().toString();

            requestMappingInfo.getDirectPaths();
            final List<String> paths = requestMappingInfo.getDirectPaths().stream().toList(); // Simplified as Condition 'requestMappingInfo. getDirectPaths() != null' is always 'true' (Earlier -paths = requestMappingInfo.getDirectPaths() != null


			final String methods =
					requestMappingInfo.getMethodsCondition().toString(); //
		
			paths.forEach(path -> {
				// Exclude Spring Boot's default error routes
				if (!path.startsWith("/error")) {
					routes.add("Method: " + methods + " | Path: " + path + " | Handler: " + methodInfo);
				}
			});
		});

		routes.sort(null);
		return routes;
	}
}