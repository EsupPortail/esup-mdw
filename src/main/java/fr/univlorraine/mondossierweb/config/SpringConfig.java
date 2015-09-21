/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import fr.univlorraine.mondossierweb.Initializer;

/**
 * Configuration Spring
 * 
 * @author Adrien Colson
 */
@Configuration
@EnableSpringConfigured
@ComponentScan(basePackageClasses=Initializer.class)
@EnableAspectJAutoProxy(proxyTargetClass=true)
@EnableWebMvc
@PropertySource("classpath:/app.properties")
public class SpringConfig {
	
	@Resource
	private Environment environment;

	/**
	 * Ajoute les paramètres de contexte aux propriétés Spring
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Messages de l'application
	 * @return
	 */
	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		resourceBundleMessageSource.setBasenames("i18n/messages", "i18n/vaadin-messages");
		return resourceBundleMessageSource;
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
	
	@Bean
	public LdapUserSearch ldapEtudiantSearch() {
		return new FilterBasedLdapUserSearch("ou=people", environment.getProperty("attributLdapCodEtu")+"={0}", ldapServer());
	}

}
