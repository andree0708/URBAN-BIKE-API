package com.urbanbike.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	@Profile("dev")
	SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

	@Bean
	@Profile("prod")
	SecurityFilterChain prodSecurityFilterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter)
			throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/health").permitAll()
						.anyRequest().permitAll())
				.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

}
