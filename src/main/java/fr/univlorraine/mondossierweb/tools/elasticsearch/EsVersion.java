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
package fr.univlorraine.mondossierweb.tools.elasticsearch;

/** Versions d'Elasticsearch gérées par l'intégration REST. */
public enum EsVersion {

	/** Elasticsearch 2.x : types ES disponibles (`_type`). */
	V2,
	/** Elasticsearch 8.x / 9.x : pas de types, champ de type dans le document. */
	V8;

	/**
	 * Déduit la version à partir du numéro renvoyé par `GET /` (`version.number`).
	 * @throws IllegalArgumentException si la version n'est pas gérée (ni 2.x, ni >= 8).
	 */
	public static EsVersion fromNumber(final String number) {
		if (number == null || !number.matches("\\d+.*")) {
			throw new IllegalArgumentException("Numéro de version Elasticsearch invalide : " + number);
		}
		final int major = Integer.parseInt(number.split("\\.")[0]);
		if (major == 2) {
			return V2;
		}
		if (major >= 8) {
			return V8;
		}
		throw new IllegalArgumentException("Version Elasticsearch non supportée : " + number);
	}
}
