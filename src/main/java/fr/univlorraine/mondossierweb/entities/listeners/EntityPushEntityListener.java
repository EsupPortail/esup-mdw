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
package fr.univlorraine.mondossierweb.entities.listeners;

import fr.univlorraine.tools.vaadin.EntityPusher;
import fr.univlorraine.tools.vaadin.EntityPusher.EntityAction;
import jakarta.annotation.Resource;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Appelle les méthodes des EntityPusher correspondants lors de l'insertion, la modification ou la suppression d'une entité.
 * @author Adrien Colson
 */
@Configurable
public class EntityPushEntityListener {

	private Logger logger = LoggerFactory.getLogger(EntityPushEntityListener.class);

	@Resource
	private ApplicationContext applicationContext;

	@PostPersist
	public void postPersist(Object entity) {
		notifyEntityPushers(EntityAction.PERSISTED, entity);
	}

	@PostUpdate
	public void postUpdate(Object entity) {
		notifyEntityPushers(EntityAction.UPDATED, entity);
	}

	@PostRemove
	public void postRemove(Object entity) {
		notifyEntityPushers(EntityAction.REMOVED, entity);
	}

	public void notifyEntityPushers(EntityAction entityAction, Object entity) {
		applicationContext.getBeansOfType(EntityPusher.class).values()
			.stream().filter(entityPusher -> entityPusher.getEntityType().isInstance(entity))
			.forEach(entityPusher -> {
				try {
					Method notifyMethod = entityPusher.getClass().getDeclaredMethod("notifyAll", EntityAction.class, Object.class);
					notifyMethod.invoke(entityPusher, entityAction, entity);
				} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					logger.error("Erreur lors de la notification de {} sur {}.", entityAction, entity, e);
				}
			});
	}

}
