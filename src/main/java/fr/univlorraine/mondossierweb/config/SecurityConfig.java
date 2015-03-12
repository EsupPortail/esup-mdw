package fr.univlorraine.mondossierweb.config;

import java.util.UUID;

import javax.annotation.Resource;

import net.sf.ehcache.CacheManager;

import org.esupportail.portal.ws.client.support.uportal.CachingUportalServiceImpl;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
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
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import fr.univlorraine.mondossierweb.utils.EmptyJMeterAuthenticationProvider;

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
				.headers().frameOptions().disable()
			.authorizeRequests()
				.antMatchers(environment.getRequiredProperty("switchUser.switchUrl")).hasRole(environment.getRequiredProperty("role.admin"))
				.antMatchers(environment.getRequiredProperty("switchUser.exitUrl")).hasAuthority(SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR)
				.anyRequest().authenticated()
				.and()
			.addFilterBefore(singleSignOutFilter(), LogoutFilter.class)
			.addFilter(new LogoutFilter(environment.getRequiredProperty("cas.url") + "/logout", new SecurityContextLogoutHandler()))
			.addFilter(casAuthenticationFilter())
			//.addFilterAfter(switchUserFilter(), FilterSecurityInterceptor.class)
			// La protection Spring Security contre le Cross Scripting Request Forgery est désactivée, Vaadin implémente sa propre protection
			.csrf().disable();
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
		casServiceProperties.setService(environment.getRequiredProperty("app.url") + "/j_spring_cas_security_check");
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
		LdapUserDetailsService ldapUserDetailsService = new LdapUserDetailsService(ldapUserSearch());
		LdapUserDetailsMapper userDetailsMapper = new LdapUserDetailsMapper();
		userDetailsMapper.setRoleAttributes(new String[] {environment.getRequiredProperty("ldap.roleAttribute")});
		userDetailsMapper.setConvertToUpperCase(false);
		ldapUserDetailsService.setUserDetailsMapper(userDetailsMapper);

		return ldapUserDetailsService;
	}

	@Bean
	public LdapContextSource ldapServer() {
		LdapContextSource ldapContextSource = new LdapContextSource();
		ldapContextSource.setUrl(environment.getRequiredProperty("ldap.url"));

		String userDn = environment.getProperty("ldap.userDn");
		if (userDn instanceof String && !userDn.isEmpty()) {
			ldapContextSource.setUserDn(userDn);
		}

		String password = environment.getProperty("ldap.password");
		if (password instanceof String && !password.isEmpty()) {
			ldapContextSource.setPassword(password);
		}

		return ldapContextSource;
	}

	@Bean
	public LdapUserSearch ldapUserSearch() {
		return new FilterBasedLdapUserSearch("ou=people", "uid={0}", ldapServer());
	}

	/* Filtre permettant de prendre le rôle d'un autre utilisateur => NON UTILISE */
	/*@Bean
	public SwitchUserFilter switchUserFilter() throws Exception {
		SwitchUserFilter switchUserFilter = new SwitchUserFilter();
		switchUserFilter.setUserDetailsService(userDetailsServiceBean());
		switchUserFilter.setSwitchUserUrl(environment.getRequiredProperty("switchUser.switchUrl"));
		switchUserFilter.setExitUserUrl(environment.getRequiredProperty("switchUser.exitUrl"));
		switchUserFilter.setTargetUrl("/");
		return switchUserFilter;
	}*/
	
	

}
