package co.com.crediya.api;

import co.com.crediya.api.dtos.LoanRequestsDTO;
import co.com.crediya.api.exceptions.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "Solicitud", description = "API para gestión de solicitudes")
public class RouterRest {
    @Bean
    @RouterOperations({
        @RouterOperation(
                path = "/api/v1/solicitud",
                method = {org.springframework.web.bind.annotation.RequestMethod.POST},
                beanClass = Handler.class,
                beanMethod = "registerRequest",
                operation = @Operation(
                        operationId = "registerRequest",
                        summary = "Registrar una nueva solicitud",
                        description = "Crea una nueva solicitud con la información proporcionada",
                        tags = {"Solicitud"},
                        requestBody = @RequestBody(
                                description = "Datos de la solicitud a registrar",
                                required = true,
                                content = @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = LoanRequestsDTO.class)
                                )
                        ),
                        responses = {
                                @ApiResponse(
                                        responseCode = "200",
                                        description = "Solicitud registrada exitosamente",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = LoanRequestsDTO.class)
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Datos de entrada inválidos",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = ErrorResponse.class)
                                        )
                                )
                        }
                )
        )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitud"), handler::registerRequest)
                .andRoute(GET("/api/v1/solicitud"), handler::getAllRequests);
    }
}
