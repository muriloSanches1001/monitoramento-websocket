package br.com.monitoramento.weblumio.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponseDTO {

    private Enum<?> code;
    private String message;
    private HttpStatus httpStatus;
    private Object data;

    public ApiResponseDTO(Enum<?> code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
