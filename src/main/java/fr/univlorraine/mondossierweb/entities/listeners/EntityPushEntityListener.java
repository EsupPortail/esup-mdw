/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.listeners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import fr.univlorraine.tools.vaadin.EntityPusher;
import fr.univlorraine.tools.vaadin.EntityPusher.EntityAction;

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
