/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.config;

import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.security.SecurityUtil;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.util.List;
import java.util.UUID;

/**
 * Configuration Spring Security (CAS).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {

	public static final String LOGOUT_URL = "/logout";

	@Resource
	private Environment environment;

	@Resource
	private MdwUserDetailsService mdwUserDetailsService;

	@Bean
	public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
		final PathPatternRequestMatcher.Builder requestMatcher = PathPatternRequestMatcher.withDefaults();
		http.authorizeHttpRequests(authz -> authz
			/* Requêtes internes Vaadin */
			.requestMatchers(SecurityUtil::isFrameworkInternalRequest).permitAll()
			.requestMatchers(requestMatcher.matcher("/VAADIN/**")).permitAll()
			/* Ressources statiques (CSS/JS/images servis en HTTP, plus bundlés) */
			.requestMatchers(requestMatcher.matcher("/css/**"), requestMatcher.matcher("/js/**"),
				requestMatcher.matcher("/images/**"), requestMatcher.matcher("/favicon.ico")).permitAll()
			/* Gestionnaire d'erreurs Spring */
			.requestMatchers(requestMatcher.matcher("/error")).permitAll()
			/* Les autres requêtes doivent être authentifiées */
			.anyRequest().authenticated());

		http.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(casEntryPoint()));

		/* Si on doit activer le rate-limiter sur /login/cas */
		if (PropertyUtils.getRateLimiterMaxRequest() > 0) {
			http.addFilterBefore(new RateLimitFilter(PropertyUtils.getRateLimiterMaxRequest()), CasAuthenticationFilter.class);
		}
		http.addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);
		http.addFilter(casAuthenticationFilter());

		/* Supprime le "continue" de l'url */
		http.requestCache(rq -> rq.requestCache(requestCache()));

		/* Désactive CSRF uniquement pour les requêtes Vaadin (qui a son propre mécanisme) */
		http.csrf(csrf -> csrf
			.ignoringRequestMatchers(SecurityUtil::isFrameworkInternalRequest)
			.ignoringRequestMatchers(requestMatcher.matcher("/VAADIN/dynamic/resource/**")));

		/* Interdit l'affichage en iFrame */
		http.headers(h -> h
			.frameOptions(o -> o.sameOrigin())
			.cacheControl(c -> c.disable()));

		/* Gestion de la déconnexion : invalide la session puis redirige vers le logout CAS */
		http.logout(logout -> {
			logout.logoutRequestMatcher(requestMatcher.matcher(LOGOUT_URL));
			logout.logoutSuccessHandler((request, response, authentication) -> {
				final SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
				/*
				 * Redirection systématique vers le logout CAS, quelle que soit
				 * l'authentification courante : on ne doit jamais revenir sur
				 * l'application, sinon le SSO CAS reconnecte automatiquement
				 * l'utilisateur tant que sa session CAS est encore valide.
				 */
				handler.setDefaultTargetUrl(environment.getRequiredProperty("cas.url") + LOGOUT_URL);
				handler.onLogoutSuccess(request, response, authentication);
			});
		});

		return http.build();
	}

	/**
	 * N'enregistre pas les requêtes internes Vaadin.
	 */
	@Bean
	public RequestCache requestCache() {
		final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
		requestCache.setRequestMatcher(request -> !SecurityUtil.isFrameworkInternalRequest(request));
		requestCache.setMatchingRequestParameterName(null);
		return requestCache;
	}

	/* Configuration CAS */
	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		final SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		singleSignOutFilter.setIgnoreInitConfiguration(true);
		return singleSignOutFilter;
	}

	@Bean
	public ServiceProperties casServiceProperties() {
		final ServiceProperties casServiceProperties = new ServiceProperties();
		casServiceProperties.setService(environment.getRequiredProperty("app.url") + Utils.CAS_RETURN_URL);
		casServiceProperties.setSendRenew(false);
		return casServiceProperties;
	}

	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		final CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setServiceProperties(casServiceProperties());
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		return casAuthenticationFilter;
	}

	@Bean
	public CasAuthenticationEntryPoint casEntryPoint() {
		final CasAuthenticationEntryPoint casEntryPoint = new CasAuthenticationEntryPoint();
		casEntryPoint.setLoginUrl(environment.getRequiredProperty("cas.url") + Utils.CAS_LOGIN_URL);
		casEntryPoint.setServiceProperties(casServiceProperties());
		return casEntryPoint;
	}

	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		final CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setKey(UUID.randomUUID().toString());
		casAuthenticationProvider.setAuthenticationUserDetailsService(new UserDetailsByNameServiceWrapper<CasAssertionAuthenticationToken>(mdwUserDetailsService));
		casAuthenticationProvider.setServiceProperties(casServiceProperties());
		casAuthenticationProvider.setTicketValidator(new Cas20ServiceTicketValidator(environment.getRequiredProperty("cas.url")));
		return casAuthenticationProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(List.of(casAuthenticationProvider()));
	}
}
