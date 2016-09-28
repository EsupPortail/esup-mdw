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

import java.util.Properties;

import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.univlorraine.mondossierweb.controllers.AdresseController;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.repositories.mdw.FavorisRepository;

/**
 * Configuration JPA
 * 
 * @author Adrien Colson
 */
@Configuration
@EnableTransactionManagement(mode=AdviceMode.ASPECTJ)
@EnableJpaRepositories(basePackageClasses=FavorisRepository.class,transactionManagerRef="transactionManager", entityManagerFactoryRef="entityManagerFactory")
public class JpaConfig {

	public final static String PERSISTENCE_UNIT_NAME = "pun-jpa";

	private Logger LOG = LoggerFactory.getLogger(JpaConfig.class);

	/**
	 * Source de données
	 * @return
	 */
	@Bean
	public DataSource dataSource() {
		JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		return dsLookup.getDataSource("java:/comp/env/jdbc/dbMdw");
	}


	/**
	 * @return Execute la migration flyway
	 */
	@Bean
	@DependsOn("dataSource")
	public Flyway flyway() {
		try{
			LOG.info("Database analysis: in progress...");
			Flyway flyway = new Flyway();
			flyway.setDataSource(dataSource());
			flyway.setBaselineOnMigrate(true);
			flyway.setValidateOnMigrate(true);
			flyway.repair();
			flyway.migrate();
			LOG.info("Database analysis: finish...");
			return flyway;
		}catch (Exception e){
			LOG.error("Database analysis: ERROR",e);
			throw e;
		}
	}

	/**
	 * EntityManager Factory
	 * @return
	 */
	@Bean
	@DependsOn("flyway")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		localContainerEntityManagerFactoryBean.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
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
