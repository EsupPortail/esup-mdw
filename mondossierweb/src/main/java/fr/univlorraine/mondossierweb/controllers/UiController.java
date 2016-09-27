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

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.views.windows.ConfirmWindow;
import fr.univlorraine.mondossierweb.views.windows.InputWindow;

/**
 * Gestion des sessions
 */
@Component
public class UiController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient LockController lockController;

	/** Thread pool  */
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	/* Envoi de messages aux clients connectés */

	/** UIs connectées */
	private LinkedList<MainUI> uis = new LinkedList<MainUI>();

	@SuppressWarnings("unchecked")
	public synchronized LinkedList<MainUI> getUis() {
		return (LinkedList<MainUI>) uis.clone();
	}

	/**
	 * Ajoute une UI à la liste des UIs connectées
	 * @param ui
	 */
	public synchronized void registerUI(final MainUI ui) {
		uis.add(ui);

		/* Met à jour les AdminViews lorsqu'une UI change de vue */
		ui.getNavigator().addViewChangeListener(new ViewChangeListener() {
			private static final long serialVersionUID = -23117484566254727L;

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
			}
		});
	}

	/**
	 * Enlève une UI de la liste des UIs connectées
	 * @param ui
	 */
	public synchronized void unregisterUI(MainUI ui) {
		uis.remove(ui);
	}

	/**
	 * Envoie une notification à tous les clients connectés
	 * @param notification
	 */
	public synchronized void sendNotification(Notification notification) {
		uis.forEach(ui ->
			executorService.execute(() ->
				ui.access(() -> notification.show(ui.getPage()))
			)
		);
	}

	/**
	 * Permet la saisie et l'envoi d'un message à tous les clients connectés
	 */
	public void sendMessage() {
		InputWindow inputWindow = new InputWindow(applicationContext.getMessage("admin.sendMessage.message", null, UI.getCurrent().getLocale()), applicationContext.getMessage("admin.sendMessage.title", null, UI.getCurrent().getLocale()));
		inputWindow.addBtnOkListener(text -> {
			if (text instanceof String && !text.isEmpty()) {
				Notification notification = new Notification(applicationContext.getMessage("admin.sendMessage.notificationCaption", new Object[] {text}, UI.getCurrent().getLocale()), Type.TRAY_NOTIFICATION);
				notification.setDelayMsec(-1);
				notification.setDescription("\n" + applicationContext.getMessage("admin.sendMessage.notificationDescription", null, UI.getCurrent().getLocale()));
				notification.setPosition(Position.TOP_CENTER);
				sendNotification(notification);
			}
		});
		UI.getCurrent().addWindow(inputWindow);
	}









	/**
	 * Vérifie si une UI est toujours active
	 * @param ui
	 * @return
	 */
	public synchronized boolean isUIStillActive(UI ui) {
		return uis.contains(ui);
	}

	/* Tuer des UIs, sessions et utilisateurs */

	/**
	 * Confirme la fermeture de toutes les sessions associées à un utilisateur
	 * @param user
	 */
	public void confirmKillUser(UserDetails user) {
		ConfirmWindow confirmWindow = new ConfirmWindow(applicationContext.getMessage("admin.uiList.confirmKillUser", new Object[]{user.getUsername()}, UI.getCurrent().getLocale()));
		confirmWindow.addBtnOuiListener(e -> killUser(user));
		UI.getCurrent().addWindow(confirmWindow);
	}

	/**
	 * Ferme toutes les sessions associées à un utilisateur
	 * @param user
	 */
	public synchronized void killUser(UserDetails user) {
		for (MainUI mainUI : uis) {
			SecurityContext securityContext = (SecurityContext) mainUI.getSession().getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
			if (user.getUsername().equals(securityContext.getAuthentication().getName())) {
				mainUI.close();
			}
		}
	}

	/**
	 * Confirme la fermeture d'une session
	 * @param session
	 */
	public void confirmKillSession(VaadinSession session) {
		SecurityContext securityContext = (SecurityContext) session.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		ConfirmWindow confirmWindow = new ConfirmWindow(applicationContext.getMessage("admin.uiList.confirmKillSession", new Object[]{session.getSession().getId(), securityContext.getAuthentication().getName()}, UI.getCurrent().getLocale()));
		confirmWindow.addBtnOuiListener(e -> killSession(session));
		UI.getCurrent().addWindow(confirmWindow);
	}

	/**
	 * Ferme une session
	 * @param session
	 */
	public void killSession(VaadinSession session) {
		session.close();
	}

	/**
	 * Confirme la fermeture d'une UI
	 * @param ui
	 */
	public void confirmKillUI(UI ui) {
		ConfirmWindow confirmWindow = new ConfirmWindow(applicationContext.getMessage("admin.uiList.confirmKillUI", null, UI.getCurrent().getLocale()));
		confirmWindow.addBtnOuiListener(e -> killUI(ui));
		UI.getCurrent().addWindow(confirmWindow);
	}

	/**
	 * Ferme une UI
	 * @param ui
	 */
	public void killUI(UI ui) {
		ui.close();
	}

	/**
	 * Supprime un verrou
	 * @param obj
	 */
	public void confirmRemoveLock(Object lock) {
		ConfirmWindow confirmWindow = new ConfirmWindow(applicationContext.getMessage("admin.uiList.confirmRemoveLock", new Object[]{lock}, UI.getCurrent().getLocale()));
		confirmWindow.addBtnOuiListener(e -> lockController.removeLock(lock));
		UI.getCurrent().addWindow(confirmWindow);
	}

}
