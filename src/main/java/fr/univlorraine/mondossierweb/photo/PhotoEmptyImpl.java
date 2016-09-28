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

import org.springframework.stereotype.Component;



/**
 * classe pour la gestion des photos .
 * @author Charlie Dubois
 */
@Component(value="photoEmptyImpl")
public class PhotoEmptyImpl implements IPhoto {

	/**
	 * constructeur vide.
	 */
	public PhotoEmptyImpl() {
		super();
	}
	
	/**
	 * ne retourne aucune photo.
	 * @param cod_ind 
	 * @return aucune photo.
	 */
	public String getUrlPhoto(String cod_ind, String cod_etu,boolean isUtilisateurEnseignant, String loginUser) {
			return null;
	}

	/**
	 * ne retourne aucune photo.
	 * @param cod_ind 
	 * @return aucune photo.
	 */
	public String getUrlPhotoTrombinoscopePdf(String cod_ind, String cod_etu,boolean isUtilisateurEnseignant, String loginUser) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOperationnel() {
		return false;
	}


}
