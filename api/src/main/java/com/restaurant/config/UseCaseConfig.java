package com.restaurant.config;

import com.restaurant.application.interactor.AuthInteractor;
import com.restaurant.domain.port.PasswordEncoderPort;
import com.restaurant.domain.port.TokenBlacklistRepository;
import com.restaurant.domain.port.TokenGeneratorPort;
import com.restaurant.domain.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public AuthInteractor authInteractor(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenGeneratorPort tokenGenerator,
            TokenBlacklistRepository tokenBlacklistRepository) {
        return new AuthInteractor(userRepository, passwordEncoder, tokenGenerator, tokenBlacklistRepository);
    }
}
