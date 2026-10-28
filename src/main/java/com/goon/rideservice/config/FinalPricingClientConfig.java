package com.goon.rideservice.config;

import com.goon.grpc.pricing.PricingServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.grpc.client.GrpcChannelBuilderCustomizer;
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@ImportGrpcClients(target = "pricing", types = PricingServiceGrpc.PricingServiceBlockingStub.class)
public class FinalPricingClientConfig {
    @Bean
    OAuth2AuthorizedClientManager finalPricingAuthorizedClientManager(
            ClientRegistrationRepository registrations, OAuth2AuthorizedClientService clients) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clients);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials().build());
        return manager;
    }

    @Bean
    GrpcChannelBuilderCustomizer<?> finalPricingBearerToken(OAuth2AuthorizedClientManager manager) {
        return GrpcChannelBuilderCustomizer.matching("pricing", builder ->
                builder.intercept(new BearerTokenAuthenticationInterceptor((java.util.function.Supplier<String>) () -> {
                    var client = manager.authorize(OAuth2AuthorizeRequest
                            .withClientRegistrationId("goon-final-pricing").principal("ride-service").build());
                    if (client == null || client.getAccessToken() == null) {
                        throw new IllegalStateException("Pricing service token unavailable");
                    }
                    return client.getAccessToken().getTokenValue();
                })));
    }
}

