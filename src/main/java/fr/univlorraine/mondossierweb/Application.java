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
package fr.univlorraine.mondossierweb;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.UI;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application Spring Boot / Vaadin Flow.
 */
@SuppressWarnings("serial")
@SpringBootApplication
@EnableScheduling
@Push(transport = Transport.WEBSOCKET_XHR)
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("css/styles.css")
public class Application extends SpringBootServletInitializer implements AppShellConfigurator, VaadinServiceInitListener {

	/**
	 * Configure le déploiement dans un container web. (WAR)
	 */
	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
		return build(builder).sources(Application.class);
	}

	@Override
	public void configurePage(final AppShellSettings settings) {
		settings.addFavIcon("icon", "images/favicon.ico", "16x16");
		settings.addLink("shortcut icon", "images/favicon.ico");
	}

	/**
	 * Désactive le thème par défaut de l'indicateur de chargement pour le remplacer
	 * par le style défini dans css/styles.css (barre visible en haut de page).
	 */
	@Override
	public void serviceInit(final ServiceInitEvent serviceInitEvent) {
		serviceInitEvent.getSource().addUIInitListener(uiInitEvent -> {
			final UI ui = uiInitEvent.getUI();
			ui.getLoadingIndicatorConfiguration().setApplyDefaultTheme(false);
		});
	}

	/**
	 * Configure le lancement de l'application via un serveur web embarqué.
	 */
	public static void main(final String[] args) {
		build(new SpringApplicationBuilder(Application.class)).run(args);
	}

	private static SpringApplicationBuilder build(final SpringApplicationBuilder builder) {
		/* Charge les propriétés par défaut à partir de application-defaults.yml */
		return builder.profiles("defaults");
	}
}
