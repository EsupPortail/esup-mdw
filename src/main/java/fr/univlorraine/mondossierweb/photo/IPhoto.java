/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
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
