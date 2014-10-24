package fr.univlorraine.mondossierweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import fr.univlorraine.mondossierweb.Initializer;

/**
 * Configuration Spring
 * 
 * @author Adrien Colson
 */
@Configuration
@EnableSpringConfigured
/* Activation des profils: décommenter cette zone et commenter la ligne suivante */
/*
@ComponentScan(basePackageClasses=Initializer.class,basePackages="fr.univlorraine")
*/
@ComponentScan(basePackageClasses=Initializer.class)
@EnableAspectJAutoProxy(proxyTargetClass=true)
@EnableWebMvc
@PropertySource("classpath:/app.properties")
public class SpringConfig {

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
		/* Activation des profils: décommenter cette zone et commenter la ligne suivante */
		/*
		resourceBundleMessageSource.setBasenames("i18n/messages", "i18n/security-messages", "i18n/vaadin-messages");
		 */
		resourceBundleMessageSource.setBasenames("i18n/messages", "i18n/vaadin-messages");
		return resourceBundleMessageSource;
	}

}
