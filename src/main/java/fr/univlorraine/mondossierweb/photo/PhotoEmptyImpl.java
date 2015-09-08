/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
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
