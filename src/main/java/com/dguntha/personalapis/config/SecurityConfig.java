package com.dguntha.personalapis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jwkSetUri;
       @Bean
     public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation (jwkSetUri);
    }
  //@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
  //private String jwkSetUri;

   /* @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).jwsAlgorithm(SignatureAlgorithm.RS256).build();
    } */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable().authorizeHttpRequests()
                .requestMatchers("/v3/**")
                .permitAll()
                .requestMatchers("/swagger-ui/**")
                .permitAll()
                .requestMatchers("/workflow/**")
                .permitAll()
                .requestMatchers("/transaction/update")
                .permitAll()
                .requestMatchers("/transaction/{id}")
                .permitAll()
                .requestMatchers("/document-type/metadata/find/{name}")
                .permitAll()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/**")
                .authenticated()
                .and()
              /*  .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                        )
                )*/
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())) 
                /*.oauth2ResourceServer().jwt().decoder(jwtDecoder())

                ; */

/*                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                        )
                )*/
           .build();
    }
}
