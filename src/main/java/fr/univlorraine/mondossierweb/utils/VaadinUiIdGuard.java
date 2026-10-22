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
package fr.univlorraine.mondossierweb.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Contournement de la CVE-2026-110803 (fuite du verrou de session Vaadin 8).
 * <p>
 * Dans {@code VaadinService.requestEnd()}, le paramètre {@code v-uiId} est lu et converti
 * en entier alors que le verrou de la {@code VaadinSession} est déjà acquis. Une valeur non
 * numérique lève une {@code NumberFormatException} avant le {@code finally} qui libère le
 * verrou : le thread du conteneur retourne au pool en le conservant, et toute requête
 * ultérieure sur cette session se bloque définitivement.
 * <p>
 * En attendant la montée de version vers Vaadin 8.32.2, les requêtes portant un
 * {@code v-uiId} non entier sont rejetées avant d'atteindre Vaadin.
 *
 * @see <a href="https://vaadin.com/security/cve-2026-110803">CVE-2026-110803</a>
 */
public final class VaadinUiIdGuard {

	/** Nom du paramètre de requête véhiculant l'identifiant d'UI. */
	static final String UI_ID_PARAMETER = "v-uiId";

	private VaadinUiIdGuard() {
	}

	/**
	 * Rejette la requête (HTTP 400) si son paramètre {@code v-uiId} n'est pas un entier.
	 *
	 * @param request  requête à contrôler
	 * @param response réponse utilisée pour le rejet
	 * @return {@code true} si la requête a été rejetée, {@code false} si elle doit être traitée normalement
	 * @throws IOException si l'envoi de l'erreur échoue
	 */
	public static boolean rejectIfInvalidUiId(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {
		final String uiId = request.getParameter(UI_ID_PARAMETER);
		if (uiId == null || isInteger(uiId)) {
			return false;
		}
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + UI_ID_PARAMETER + " parameter");
		return true;
	}

	private static boolean isInteger(final String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
