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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;
import fr.univlorraine.mondossierweb.security.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestion des sessions et des UIs connectées.
 *
 * Version Flow : les UIs sont enregistrées/désenregistrées par le layout principal
 * (MainLayout) via {@link #registerUI(UI)} / {@link #unregisterUI(UI)}.
 * Les confirmations (ConfirmWindow v7) sont désormais à la charge des vues :
 * la vue affiche un ConfirmDialog puis appelle directement killUser/killSession/killUI.
 */
@Component
public class UiController {

	/** Thread pool  */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	/* Envoi de messages aux clients connectés */

	/** UIs connectées */
	private final LinkedList<UI> uis = new LinkedList<>();

	@SuppressWarnings("unchecked")
	public synchronized List<UI> getUis() {
		return (List<UI>) uis.clone();
	}

	/**
	 * Ajoute une UI à la liste des UIs connectées
	 * @param ui
	 */
	public synchronized void registerUI(final UI ui) {
		uis.add(ui);
	}

	/**
	 * Enlève une UI de la liste des UIs connectées
	 * @param ui
	 */
	public synchronized void unregisterUI(UI ui) {
		uis.remove(ui);
	}

	/**
	 * Envoie une notification à tous les clients connectés
	 * @param message texte de la notification
	 */
	public synchronized void sendNotification(String message) {
		uis.forEach(ui ->
			executorService.execute(() ->
				ui.access(() -> {
					Notification notification = new Notification(message);
					notification.setDuration(0);
					notification.setPosition(Notification.Position.TOP_CENTER);
					notification.open();
				})
			)
		);
	}

	/**
	 * Vérifie si une UI est toujours active
	 * @param ui
	 * @return
	 */
	public synchronized boolean isUIStillActive(UI ui) {
		return uis.contains(ui) && ui.isAttached();
	}

	/* Tuer des UIs, sessions et utilisateurs */

	/**
	 * Ferme toutes les sessions associées à un utilisateur
	 * @param user
	 */
	public synchronized void killUser(UserDetails user) {
		for (UI ui : uis) {
			String username = SecurityUtil.getAuthentication(ui).map(Authentication::getName).orElse(null);
			if (user.getUsername().equals(username)) {
				VaadinSession session = ui.getSession();
				ui.access(() -> {
					if (session != null) {
						session.close();
					}
					ui.getPage().reload();
				});
			}
		}
	}

	/**
	 * Ferme une session
	 * @param session
	 */
	public void killSession(VaadinSession session) {
		session.access(session::close);
	}

	/**
	 * Ferme une UI
	 * @param ui
	 */
	public void killUI(UI ui) {
		ui.access(ui::close);
	}

}
