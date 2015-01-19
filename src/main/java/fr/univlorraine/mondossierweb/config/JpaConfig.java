package fr.univlorraine.mondossierweb.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.univlorraine.mondossierweb.entities.Favoris;
import fr.univlorraine.mondossierweb.repositories.FavorisRepository;

/**
 * Configuration JPA
 * 
 * @author Adrien Colson
 */
@Configuration
@EnableTransactionManagement(mode=AdviceMode.ASPECTJ)
/* Activation des profils: décommenter cette zone et commenter la ligne suivante */
/*
@EnableJpaRepositories(basePackageClasses={StructureRepository.class, SecurityProfilRepository.class})
*/
@EnableJpaRepositories(basePackageClasses=FavorisRepository.class,transactionManagerRef="transactionManager", entityManagerFactoryRef="entityManagerFactory")
public class JpaConfig {

	public final static String PERSISTENCE_UNIT_NAME = "pun-jpa";

	/**
	 * Source de données
	 * @return
	 */
	@Bean
	public DataSource dataSource() {
		JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		return dsLookup.getDataSource("java:/comp/env/jdbc/db");
	}

	/**
	 * Initialisation de la base de données
	 * @param schemaScript
	 * @param dataScript
	 * @return
	 */
	/*Activation des profils: décommenter cette zone et commenter la ligne suivante
	 * -->insertion des donn�es exemple*/
	/*	
	public DataSourceInitializer dataSourceInitializer(@Value("classpath:dataBase/db-schema.sql") org.springframework.core.io.Resource schemaScript, @Value("classpath:dataBase/db-test-data.sql") org.springframework.core.io.Resource dataScript, @Value("classpath:dataBase/db-security.sql") org.springframework.core.io.Resource securityScript) {
	*/
	/*@Bean
	public DataSourceInitializer dataSourceInitializer(@Value("classpath:dataBase/db-schema.sql") org.springframework.core.io.Resource schemaScript, @Value("classpath:dataBase/db-test-data.sql") org.springframework.core.io.Resource dataScript) {
		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(dataSource());

		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
		resourceDatabasePopulator.addScript(schemaScript);
		resourceDatabasePopulator.addScript(dataScript);
		//Activation des profils: décommenter cette zone
		//resourceDatabasePopulator.addScript(securityScript);
		dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

		return dataSourceInitializer;
	}*/

	/**
	 * EntityManager Factory
	 * @return
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		localContainerEntityManagerFactoryBean.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
		/* Activation des profils: décommenter cette zone et commenter la ligne suivante */
		/*
		localContainerEntityManagerFactoryBean.setPackagesToScan(Structure.class.getPackage().getName(), SecurityProfil.class.getPackage().getName());
		*/
		localContainerEntityManagerFactoryBean.setPackagesToScan(Favoris.class.getPackage().getName());
		localContainerEntityManagerFactoryBean.setDataSource(dataSource());
		localContainerEntityManagerFactoryBean.setJpaDialect(new EclipseLinkJpaDialect());

		Properties jpaProperties = new Properties();
		/* Active le static weaving d'EclipseLink */
		jpaProperties.put(PersistenceUnitProperties.WEAVING, "static");
		/* Désactive le cache partagé */
		jpaProperties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, String.valueOf(false));
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
	public JpaTransactionManager transactionManager() {
		return new JpaTransactionManager(entityManagerFactory().getObject());
	}

}
