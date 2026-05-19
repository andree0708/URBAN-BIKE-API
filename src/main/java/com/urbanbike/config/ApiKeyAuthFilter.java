package com.urbanbike.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("prod")
public class ApiKeyAuthFilter extends OncePerRequestFilter {

	private static final String API_KEY_HEADER = "X-API-Key";

	private final String apiKey;

	public ApiKeyAuthFilter(@Value("${api.security.key}") String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if ("/health".equals(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		String providedKey = request.getHeader(API_KEY_HEADER);
		if (apiKey != null && !apiKey.isBlank() && apiKey.equals(providedKey)) {
			filterChain.doFilter(request, response);
			return;
		}

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String json = """
				{"timestamp":"%s","httpStatus":401,"error":"Unauthorized","message":"API Key inválida o ausente. Envía el header X-API-Key."}
				""".formatted(LocalDateTime.now()).trim();
		response.getWriter().write(json);
	}

}
