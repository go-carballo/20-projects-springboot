package com.alec.solution.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Inventario API")
                        .description("API REST para gestión de inventario de productos. " +
                                "Incluye funcionalidades de CRUD, control de stock, " +
                                "historial de movimientos, paginación y búsqueda avanzada.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Alec")
                                .email("alec@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo")))
                .tags(List.of(
                        new Tag().name("Productos").description("Operaciones CRUD y gestión de productos"),
                        new Tag().name("Stock").description("Control de entradas, salidas y ajustes de inventario"),
                        new Tag().name("Movimientos").description("Historial y consulta de movimientos de stock"),
                        new Tag().name("Búsqueda").description("Búsqueda y filtrado de productos")));
    }
}
