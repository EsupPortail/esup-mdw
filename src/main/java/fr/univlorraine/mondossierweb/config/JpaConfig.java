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

import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.repositories.mdw.FavorisRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Configuration JPA de la base propre à l'application (mdw).
 */
@Configuration
@EnableJpaRepositories(
	basePackageClasses = FavorisRepository.class,
	entityManagerFactoryRef = "entityManagerFactory",
	transactionManagerRef = "transactionManager")
public class JpaConfig {

	public final static String PERSISTENCE_UNIT_NAME = "pun-jpa";

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource.mdw")
	public DataSourceProperties mdwDataSourceProperties() {
		return new DataSourceProperties();
	}

	/**
	 * Source de données mdw
	 */
	@Bean
	@Primary
	public DataSource dataSource() {
		return mdwDataSourceProperties().initializeDataSourceBuilder().build();
	}

	/**
	 * Exécute la migration flyway avec réparation préalable des checksums
	 * (comportement historique de l'application).
	 */
	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy() {
		return flyway -> {
			flyway.repair();
			flyway.migrate();
		};
	}

	/**
	 * EntityManager Factory mdw
	 */
	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(final EntityManagerFactoryBuilder builder) {
		return builder
			.dataSource(dataSource())
			.packages(Favoris.class.getPackage().getName())
			.persistenceUnit(PERSISTENCE_UNIT_NAME)
			/* Conserve les noms de colonnes tels que déclarés dans les entités */
			.properties(Map.of(
				"hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",
				"hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl"))
			.build();
	}

	/**
	 * Transaction Manager mdw
	 */
	@Bean
	@Primary
	public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}
