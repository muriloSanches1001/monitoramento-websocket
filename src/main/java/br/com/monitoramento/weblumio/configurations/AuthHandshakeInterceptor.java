package br.com.monitoramento.weblumio.configurations;

import br.com.monitoramento.weblumio.services.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class AuthHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final TokenService tokenService;

    @Autowired
    public AuthHandshakeInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = request.getHeaders().getFirst("Authorization");

        // Verificar a validade do token
        if (token == null || !this.isValidToken(token)) {
            log.warn("Token inválido ou ausente.");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        log.info("Token válido. Handshake autorizado.");
        return true;
    }

    private boolean isValidToken(String token) {
        if (token.startsWith("Bearer "))
            token = token.substring(7);
        else
            return false;

        return this.tokenService.validateAccessApiToken(token) != null;
    }

}
