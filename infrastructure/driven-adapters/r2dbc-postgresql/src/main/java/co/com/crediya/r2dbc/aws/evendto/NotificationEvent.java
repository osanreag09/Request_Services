package co.com.crediya.r2dbc.aws.evendto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    @JsonProperty("solicitudId")
    private String solicitudId;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("email")
    private String email;

    @JsonProperty("mensaje")
    private String mensaje;
}