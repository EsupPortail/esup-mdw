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
package fr.univlorraine.mondossierweb.photo;


/**
 * interface de la classe photo. 
 * Liste les méthodes indispensable à la récupération des photos.
 * @author Charlie Dubois
 */
public interface IPhoto {

	/**
	 * Retourne l'url pour la photo de l'individu dont le cod_ind est placé  en paramètre.
	 * @param cod_ind
	 * @param cod_etu
	 * @param isUtilisateurEnseignant
	 * @param loginUser
	 * @return l'url pour récupérer la photo.
	 * 
	 */
	String getUrlPhoto(String cod_ind, String cod_etu,boolean isUtilisateurEnseignant, String loginUser);
	
	/**
	 * retourne l'url pour la photo de l'individu dont le cod_ind est placé en paramètre
	 * Avec une url pour le serveur et non pour l'affichage à l'écran, pour le client.
	 * @param cod_ind
	 * @param cod_etu
	 * @param isUtilisateurEnseignant
	 * @param loginUser
	 * @return l'url pour récupérer la photo
	 */
	String getUrlPhotoTrombinoscopePdf(String cod_ind, String cod_etu,boolean isUtilisateurEnseignant, String loginUser);

	boolean isOperationnel();
	
}
