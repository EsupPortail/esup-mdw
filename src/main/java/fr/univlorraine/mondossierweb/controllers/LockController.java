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
package fr.univlorraine.mondossierweb.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import fr.univlorraine.mondossierweb.security.SecurityUtil;

/**
 * Gestion des verrous
 */
@Component
public class LockController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UiController uiController;

	/** Liste des verrous */
	private final Map<Object, UI> locks = new ConcurrentHashMap<>();

	/**
	 * @param ui
	 * @return liste des verrous associés à l'ui
	 */
	public List<Object> getUILocks(UI ui) {
		List<Object> uiLocks = new ArrayList<>();
		locks.entrySet()
			.stream().filter(e -> e.getValue() == ui)
			.forEach(e -> uiLocks.add(e.getKey()));
		return uiLocks;
	}

	/**
	 * Verrouille une ressource pour l'UI courante
	 * @param obj la ressource à verrouiller
	 * @return true si la ressource est bien verrouillée pour l'UI courante, false sinon
	 */
	public boolean getLock(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj ne peut être null.");
		}

		UI lockUI = locks.get(obj);
		if (lockUI != null && lockUI != UI.getCurrent() && uiController.isUIStillActive(lockUI)) {
			return false;
		}

		locks.put(obj, UI.getCurrent());

		return true;
	}

	/**
	 * Verrouille une ressource pour l'UI courante
	 * @param obj la ressource à verrouiller
	 * @param msgIfAlreadyLocked message affiché si la ressource est déjà verrouillée pour une autre UI. Si cette propriété vaut null, un message par défaut est affiché.
	 * @return true si la ressource est bien verrouillée pour l'UI courante, false sinon
	 */
	public boolean getLockOrNotify(Object obj, String msgIfAlreadyLocked) {
		boolean ret = getLock(obj);
		if (!ret) {
			if (!StringUtils.hasText(msgIfAlreadyLocked)) {
				msgIfAlreadyLocked = applicationContext.getMessage("lock.alreadyLocked", new Object[] {obj.getClass().getSimpleName(), getUserNameFromLock(obj)}, UI.getCurrent().getLocale());
			}
			Notification notification = Notification.show(msgIfAlreadyLocked);
			notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
		}
		return ret;
	}

	/**
	 * Supprime un verrou
	 * @param obj
	 */
	public void removeLock(Object obj) {
		locks.remove(obj);
	}

	/**
	 * Rend un verrou, après avoir vérifié qu'il appartient à l'UI courante
	 * @param obj
	 */
	public void releaseLock(Object obj) {
		if (locks.get(obj) == UI.getCurrent()) {
			removeLock(obj);
		}
	}

	/**
	 * Retourne le nom de l'utilisateur pour le lock passé en paramètre
	 * @param obj
	 * @return userName
	 */
	public String getUserNameFromLock(Object obj){
		UI lockUi = locks.get(obj);
		if (lockUi != null) {
			return SecurityUtil.getAuthentication(lockUi).map(Authentication::getName).orElse(null);
		}
		return null;
	}

}
