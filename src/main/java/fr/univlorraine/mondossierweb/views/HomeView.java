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
package fr.univlorraine.mondossierweb.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * Vue d'atterrissage : redirige vers la vue adaptée au profil de l'utilisateur
 * (reprend la logique d'init de l'ancien MainUI).
 */
@SuppressWarnings("serial")
@Route(value = "", layout = MainLayout.class)
public class HomeView extends Div implements BeforeEnterObserver {

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient ConfigController configController;

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		if (!applicationActive()) {
			event.forwardTo(AccesBloqueView.class);
			return;
		}

		if (userController.isEnseignant()) {
			/* Récupération des favoris pour l'utilisateur : s'il en a, on affiche les favoris, sinon la recherche rapide */
			final List<Favoris> lfav = favorisController.getFavoris();
			if (lfav != null && !lfav.isEmpty()) {
				event.forwardTo(MainLayout.ROUTE_FAVORIS);
			} else {
				event.forwardTo(MainLayout.ROUTE_RECHERCHE_RAPIDE);
			}
			MainLayout.getCurrent().ifPresent(layout -> layout.afficherMessageIntroEnseignants(false, true));
		} else if (userController.isEtudiant()) {
			event.forwardTo(MainLayout.ROUTE_ETAT_CIVIL);
			MainLayout.getCurrent().ifPresent(layout -> layout.afficherMessageIntro(false, true));
		} else {
			event.forwardTo(ErreurView.class);
		}
	}

	private boolean applicationActive() {
		final boolean etudiant = userController.isEtudiant();
		final boolean enseignant = userController.isEnseignant();
		final boolean gestionnaire = userController.isGestionnaire();
		return configController.isApplicationActive()
			&& ((etudiant && configController.isPartieEtudiantActive())
				|| (!enseignant && !etudiant && !gestionnaire)
				|| (enseignant && configController.isPartieEnseignantActive())
				|| (gestionnaire && configController.isProfilGestionnaireActif()));
	}
}
