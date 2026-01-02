package com.alec.solution.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "basicAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Inventario API")
                        .description("""
                                API REST para gestion de inventario de productos.
                                
                                ## Funcionalidades
                                - CRUD completo de productos
                                - Control de stock (entradas, salidas, ajustes)
                                - Historial de movimientos
                                - Paginacion y busqueda avanzada
                                - Soft delete con reactivacion
                                
                                ## Autenticacion
                                La API usa autenticacion HTTP Basic. Usuarios disponibles:
                                - **viewer / viewer123** - Solo lectura (GET)
                                - **operator / operator123** - Lectura + operaciones de stock
                                - **admin / admin123** - Acceso completo
                                
                                ## Roles y Permisos
                                | Operacion | VIEWER | OPERATOR | ADMIN |
                                |-----------|--------|----------|-------|
                                | GET (consultas) | Si | Si | Si |
                                | Stock (entrada/salida/ajuste) | No | Si | Si |
                                | POST (crear) | No | No | Si |
                                | PUT (actualizar) | No | No | Si |
                                | DELETE (eliminar) | No | No | Si |
                                | Reactivar | No | No | Si |
                                """)
                        .version("3.0.0")
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
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Autenticacion HTTP Basic. Use las credenciales proporcionadas arriba.")))
                .tags(List.of(
                        new Tag().name("Productos").description("Operaciones CRUD y gestion de productos"),
                        new Tag().name("Stock").description("Control de entradas, salidas y ajustes de inventario"),
                        new Tag().name("Movimientos").description("Historial y consulta de movimientos de stock"),
                        new Tag().name("Busqueda").description("Busqueda y filtrado de productos")));
    }
}
