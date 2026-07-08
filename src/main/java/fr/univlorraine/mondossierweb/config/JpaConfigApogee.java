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

import fr.univlorraine.mondossierweb.entities.apogee.VObjSeApogee;
import fr.univlorraine.mondossierweb.entities.converters.BooleanToStringConverter;
import fr.univlorraine.mondossierweb.repositories.apogee.ComposanteApogeeRepository;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Configuration JPA de la base Apogée (Oracle, lecture seule).
 */
@Configuration
@EnableJpaRepositories(
	basePackageClasses = ComposanteApogeeRepository.class,
	entityManagerFactoryRef = "entityManagerFactoryApogee",
	transactionManagerRef = "transactionManagerApogee")
public class JpaConfigApogee {

	public final static String PERSISTENCE_UNIT_NAME = "pun-jpaApogee";

	@Bean
	@ConfigurationProperties("spring.datasource.apogee")
	public DataSourceProperties apogeeDataSourceProperties() {
		return new DataSourceProperties();
	}

	/**
	 * Source de données Apogée
	 */
	@Bean
	public DataSource dataSourceApogee() {
		return apogeeDataSourceProperties().initializeDataSourceBuilder().build();
	}

	/**
	 * EntityManager Factory Apogée
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryApogee(final EntityManagerFactoryBuilder builder) {
		return builder
			.dataSource(dataSourceApogee())
			.packages(VObjSeApogee.class.getPackage().getName(), BooleanToStringConverter.class.getPackage().getName())
			.persistenceUnit(PERSISTENCE_UNIT_NAME)
			/* Conserve les noms de colonnes tels que déclarés dans les entités */
			.properties(Map.of(
				"hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",
				"hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl",
				"hibernate.show_sql", String.valueOf(PropertyUtils.getShowSqlApogee())))
			.build();
	}

	/**
	 * Transaction Manager Apogée
	 */
	@Bean
	public PlatformTransactionManager transactionManagerApogee(
		@Qualifier("entityManagerFactoryApogee") final EntityManagerFactory entityManagerFactoryApogee) {
		return new JpaTransactionManager(entityManagerFactoryApogee);
	}
}
