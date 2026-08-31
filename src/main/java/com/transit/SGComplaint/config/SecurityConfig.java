package com.transit.SGComplaint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RoleBasedAuthenticationSuccessHandler successHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET,
                    "/complaints",
                    "/complaints/view/**",
                    "/notices/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/complaints/*/verify").permitAll()
                .requestMatchers(
                    "/",
                    "/login",
                    "/signup/**",
                    "/api/members/check-id",
                    "/api/phone-verifications/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/main-banners/**",
                    "/company/**",
                    "/route/**",
                    "/recruit/**",
                    "/customer/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                .requestMatchers("/admin/partners", "/admin/partners/**").hasRole("MASTER")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MASTER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("empId")
                .passwordParameter("empPassword")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(60 * 60 * 24 * 14)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
            );

        return http.build();
    }
}
