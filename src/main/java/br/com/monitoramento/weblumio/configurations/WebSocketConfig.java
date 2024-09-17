package br.com.monitoramento.weblumio.configurations;

import br.com.monitoramento.weblumio.handlers.SensorWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Autowired
    public WebSocketConfig(AuthHandshakeInterceptor authHandshakeInterceptor) {
        this.authHandshakeInterceptor = authHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SensorWebSocketHandler(), "/ws/sensor/{id}")
                .setAllowedOrigins("*")
                .addInterceptors(authHandshakeInterceptor);
    }

}
