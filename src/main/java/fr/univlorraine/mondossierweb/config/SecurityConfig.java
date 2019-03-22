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

import java.util.UUID;

import javax.annotation.Resource;

import org.esupportail.portal.ws.client.support.uportal.CachingUportalServiceImpl;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.security.VaadinSecurityContextHolderStrategy;
//import fr.univlorraine.mondossierweb.security.VaadinSecurityContextHolderStrategy;
import net.sf.ehcache.CacheManager;



/**
 * Configuration Spring Security
 * 
 * @author Adrien Colson
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true, jsr250Enabled=true, prePostEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Resource
	private Environment environment;

	@Resource
	private MdwUserDetailsService mdwUserDetailsService;
	
	/* Passe le SecurityContext aux threads créées et à la session Vaadin. */
	static {
		SecurityContextHolder.setStrategyName(VaadinSecurityContextHolderStrategy.class.getName());
	}

	
	@Bean(name="authenticationManager")
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.exceptionHandling()
				.authenticationEntryPoint(casEntryPoint())
				.and()
			.authorizeRequests()
				.anyRequest().authenticated()
				.and()
			.addFilterBefore(singleSignOutFilter(), LogoutFilter.class)
			.addFilter(new LogoutFilter(environment.getRequiredProperty("cas.url") + "/logout", new SecurityContextLogoutHandler()))
			.addFilter(casAuthenticationFilter())
			// La protection Spring Security contre le Cross Scripting Request Forgery est désactivée, Vaadin implémente sa propre protection
			.csrf().disable()
			.headers()
			         /* Autorise l'affichage en iFrame */
			         .frameOptions().disable()
			         /* Supprime la gestion du cache du navigateur, pour corriger le bug IE de chargement des polices cf. http://stackoverflow.com/questions/7748140/font-face-eot-not-loading-over-https */
			         .cacheControl().disable();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(casAuthenticationProvider());
	
	}

	
	/* Uportal service */
	@Bean
	public CachingUportalServiceImpl cachingUportalServiceImpl(){
		CachingUportalServiceImpl cachingUportalService= new CachingUportalServiceImpl();
		cachingUportalService.setUrl(environment.getRequiredProperty("uportal.ws"));
		cachingUportalService.setCacheManager(new CacheManager());
		return cachingUportalService;
		
	}

	/* Configuration CAS */
	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		SingleSignOutFilter filter = new SingleSignOutFilter();
		filter.setCasServerUrlPrefix(environment.getRequiredProperty("app.url"));
		return filter;
	}

	@Bean
	public ServiceProperties casServiceProperties() {
		ServiceProperties casServiceProperties = new ServiceProperties();
		casServiceProperties.setService(environment.getRequiredProperty("app.url") + "/login/cas");
		casServiceProperties.setSendRenew(false);
		return casServiceProperties;
	}

	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		return casAuthenticationFilter;
	}

	@Bean
	public CasAuthenticationEntryPoint casEntryPoint() {
		CasAuthenticationEntryPoint casEntryPoint = new CasAuthenticationEntryPoint();
		casEntryPoint.setLoginUrl(environment.getRequiredProperty("cas.url") + "/login");
		casEntryPoint.setServiceProperties(casServiceProperties());
		return casEntryPoint;
	}


	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() throws Exception {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setKey(UUID.randomUUID().toString());
		casAuthenticationProvider.setAuthenticationUserDetailsService(new UserDetailsByNameServiceWrapper<CasAssertionAuthenticationToken>(userDetailsServiceBean()));
		casAuthenticationProvider.setServiceProperties(casServiceProperties());
		casAuthenticationProvider.setTicketValidator(new Cas20ServiceTicketValidator(environment.getRequiredProperty("cas.url")));
		return casAuthenticationProvider;
	}

	
	@Bean(name="userDetailsService")
	@Override
	public UserDetailsService userDetailsServiceBean() throws Exception {
		return mdwUserDetailsService;
	}
	

}
