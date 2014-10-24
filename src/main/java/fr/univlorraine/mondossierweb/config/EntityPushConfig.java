package fr.univlorraine.mondossierweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.univlorraine.mondossierweb.entities.Structure;
import fr.univlorraine.tools.vaadin.EntityPusher;

/**
 * Configuration Entity Push
 * 
 * @author Adrien Colson
 */
@Configuration
public class EntityPushConfig {

	@Bean
	public EntityPusher<Structure> structureEntityPusher() {
		return new EntityPusher<>(Structure.class);
	}

}
