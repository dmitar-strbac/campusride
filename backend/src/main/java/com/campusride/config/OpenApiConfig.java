package com.campusride.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI campusRideOpenAPI() {
    String schemeName = "bearerAuth";

    return new OpenAPI()
        .info(
            new Info()
                .title("CampusRide API")
                .description("REST API for rides, bookings, users, and chat history")
                .version("1.0.0"))
        .addTagsItem(new Tag().name("Authentication").description("Register and sign in"))
        .addTagsItem(new Tag().name("Profile").description("Current user profile"))
        .addTagsItem(new Tag().name("Rides").description("Publish, search, and manage rides"))
        .addTagsItem(
            new Tag().name("Bookings").description("Request seats and manage booking decisions"))
        .addTagsItem(
            new Tag().name("Ride Chat").description("Message history for ride participants"))
        .components(
            new Components()
                .addSecuritySchemes(
                    schemeName,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(schemeName));
  }
}
