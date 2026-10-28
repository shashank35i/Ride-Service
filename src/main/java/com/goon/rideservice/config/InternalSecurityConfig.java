package com.goon.rideservice.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
public class InternalSecurityConfig {

    @Bean
    SecurityFilterChain internalApiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/rides/create_ride")
                        .access(fromServiceWithScope("goon-services", "rides.create"))
                        .requestMatchers(HttpMethod.POST, "/internal/rides/*/arrived")
                        .access(fromServiceWithScope("goon-services", "rides.arrive"))
                        .requestMatchers(HttpMethod.POST, "/internal/rides/*/start")
                        .access(fromServiceWithScope("goon-services", "rides.start"))
                        .requestMatchers(HttpMethod.POST, "/internal/rides/*/end")
                        .access(fromServiceWithScope("goon-services", "rides.end"))
                        .requestMatchers(HttpMethod.PATCH, "/internal/offers/*/decline")
                        .access(fromServiceWithScope("goon-services", "offers.decline"))
                        .requestMatchers(
                                HttpMethod.POST,
                                "/internal/offers/*/accept"
                        )
                        .access(fromServiceWithScope("goon-services", "offers.accept"))
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }

    private AuthorizationManager<RequestAuthorizationContext> fromServiceWithScope(
            String expectedClient, String expectedScope) {
        return (authentication, context) -> {
            Authentication current = authentication.get();
            if (!(current instanceof JwtAuthenticationToken jwtAuthentication)) {
                return new AuthorizationDecision(false);
            }
            return new AuthorizationDecision(
                    jwtAuthentication.getAuthorities().stream()
                            .anyMatch(authority -> authority.getAuthority().equals("SCOPE_" + expectedScope))
                            && expectedClient.equals(jwtAuthentication.getToken().getClaimAsString("azp")));
        };
    }
}
