/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.config;

import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.univlorraine.mondossierweb.entities.apogee.VObjSeApogee;
import fr.univlorraine.mondossierweb.repositories.apogee.ComposanteApogeeRepository;


/**
 * Configuration JPA
 * 
 * @author Charlie Dubois
 */
@Configuration
@EnableTransactionManagement(mode=AdviceMode.ASPECTJ)
@EnableJpaRepositories(basePackageClasses=ComposanteApogeeRepository.class, transactionManagerRef="transactionManagerApogee", entityManagerFactoryRef="entityManagerFactoryApogee")
public class JpaConfigApogee {

	public final static String PERSISTENCE_UNIT_NAME = "pun-jpaApogee";

	/**
	 * Source de données
	 * @return
	 */
	@Bean
	public DataSource dataSourceApogee() {
		JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		return dsLookup.getDataSource("java:/comp/env/jdbc/dbApogee");
	}


	/**
	 * EntityManager Factory
	 * @return
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryApogee() {
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		localContainerEntityManagerFactoryBean.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
		localContainerEntityManagerFactoryBean.setPackagesToScan(VObjSeApogee.class.getPackage().getName());
		localContainerEntityManagerFactoryBean.setDataSource(dataSourceApogee());
		localContainerEntityManagerFactoryBean.setJpaDialect(new EclipseLinkJpaDialect());

		Properties jpaProperties = new Properties();
		/* Active le static weaving d'EclipseLink */
		jpaProperties.put(PersistenceUnitProperties.WEAVING, "static");
		/* Désactive le cache partagé */
		jpaProperties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, String.valueOf(false));
		localContainerEntityManagerFactoryBean.setSharedCacheMode(SharedCacheMode.NONE);
		localContainerEntityManagerFactoryBean.setJpaProperties(jpaProperties);

		EclipseLinkJpaVendorAdapter jpaVendorAdapter = new EclipseLinkJpaVendorAdapter();
		jpaVendorAdapter.setGenerateDdl(false);
		jpaVendorAdapter.setShowSql(false);
		localContainerEntityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);

		return localContainerEntityManagerFactoryBean;
	}

	/**
	 * Transaction Manager
	 * @return
	 */
	@Bean
	public JpaTransactionManager transactionManagerApogee() {
		return new JpaTransactionManager(entityManagerFactoryApogee().getObject());
	}

}
