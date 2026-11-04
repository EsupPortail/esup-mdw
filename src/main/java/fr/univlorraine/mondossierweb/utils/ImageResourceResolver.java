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

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Localise les images de documents (PDF) sur le classpath.
 * <p>
 * Convention des répertoires :
 * <ul>
 *   <li>{@code src/main/resources/META-INF/resources/images/} : ressources de vue, servies
 *       par le navigateur sur {@code /images/**} ;</li>
 *   <li>{@code src/main/resources/images/} : images de documents, jamais exposées en HTTP,
 *       lues ici via le classpath ({@code images/**}).</li>
 * </ul>
 * Les valeurs de paramètres acceptées sont donc un chemin classpath ({@code images/logo.png}),
 * un nom de fichier seul ({@code logo.png}) ou un chemin web ({@code /images/logo.png}, le
 * préfixe étant alors interprété comme la racine classpath des documents).
 */
@Slf4j
public final class ImageResourceResolver {

	/** Racine classpath des images de documents. */
	public static final String DOCUMENT_IMAGE_ROOT = "images/";

	private ImageResourceResolver() {
	}

	/**
	 * Indique si la valeur configurée est une URL distante.
	 *
	 * @param value valeur du paramètre applicatif
	 * @return {@code true} pour une URL {@code http(s)} ou protocole-relative
	 */
	public static boolean isRemoteUrl(final String value) {
		if (value == null) {
			return false;
		}
		final String lower = value.trim().toLowerCase();
		return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("//");
	}

	/**
	 * Lit une image de document depuis le classpath.
	 *
	 * @param value valeur du paramètre applicatif
	 * @return le contenu de l'image, ou {@code null} si elle est absente du classpath
	 */
	public static byte[] readClasspathImage(final String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		final String location = toClasspathLocation(value);
		try (InputStream in = openClasspathResource(location)) {
			if (in == null) {
				return null;
			}
			return in.readAllBytes();
		} catch (IOException e) {
			log.warn("Lecture impossible de l'image de document '{}'", location, e);
			return null;
		}
	}

	/**
	 * Normalise une valeur de paramètre en emplacement classpath sous {@link #DOCUMENT_IMAGE_ROOT}.
	 *
	 * @param value valeur du paramètre applicatif
	 * @return l'emplacement classpath, par exemple {@code images/logo-UL.png}
	 */
	static String toClasspathLocation(final String value) {
		String path = value.trim().replace('\\', '/');
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (!path.startsWith(DOCUMENT_IMAGE_ROOT)) {
			path = DOCUMENT_IMAGE_ROOT + path;
		}
		return path;
	}

	private static InputStream openClasspathResource(final String location) {
		final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		if (contextLoader != null) {
			final InputStream in = contextLoader.getResourceAsStream(location);
			if (in != null) {
				return in;
			}
		}
		return ImageResourceResolver.class.getClassLoader().getResourceAsStream(location);
	}
}
