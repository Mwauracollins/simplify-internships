package org.simplifyinternships.simplifyinternships.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.simplifyinternships.simplifyinternships.entities.userentities.BaseUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.io.IOException;

import static org.simplifyinternships.simplifyinternships.Utils.Permission.*;
import static org.simplifyinternships.simplifyinternships.Utils.UserRole.*;
import static org.springframework.http.HttpMethod.*;

/*
This class is responsible for configuring Spring Security.
Defines how the application handles authentication, authorization and logout functions
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    // Whitelist of URLS that are allowed without authentication
    private static final String[] WHITE_LIST_URL = {
            "/",
            "/contact/**",
            "/about/**",
            "/auth/**",
            "/user/**"
    };
    @Bean
    public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity httpSecurity) throws Exception {
        //Configure security rules using HttpSecurity
        httpSecurity
                .csrf(
                        //Disable built-in CSRF protection
                        AbstractHttpConfigurer::disable
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(WHITE_LIST_URL)
                                .permitAll()//Allow access to whitelisted urls without authentication

                                // Require admin roles to access the requested url and request methods
                                .requestMatchers("/management/**").hasAnyRole(ADMIN.name(), MANAGER.name())
                                .requestMatchers(GET, "/management/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
                                .requestMatchers(POST,"/management/**").hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
                                .requestMatchers(PUT, "/management/**").hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())
                                .requestMatchers(DELETE, "/management/**").hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(
                        session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(
                        logout -> logout
                                .logoutUrl("/auth/logout")
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler(((request, response, authentication) -> SecurityContextHolder.clearContext()))
                )
//                .formLogin(
//                        formLogin -> formLogin
//                                .loginPage("/auth/login")
//                                .successHandler(authenticationSuccessHandler())
//                                .defaultSuccessUrl("/home", true)
//                )
                ;
        return httpSecurity.build();
    }

    private @NotNull AuthenticationSuccessHandler authenticationSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler(){
            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication) throws IOException {
                String targetUrl = determineTargetUrl(request, response);
                if (response.isCommitted()){
                    logger.debug("Response has already been submitted. Unable to redirect to " + targetUrl);
                    return;
                }
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            }
            @Contract(pure = true)
            protected @NotNull String determineTargetUrl(HttpServletRequest request, HttpServletResponse response){
                //TODO: Add customizations to redirect base on userroles
                return "/home";
            }
        };

        handler.setDefaultTargetUrl("/home");
        handler.setUseReferer(true);

        return handler;
    }
}