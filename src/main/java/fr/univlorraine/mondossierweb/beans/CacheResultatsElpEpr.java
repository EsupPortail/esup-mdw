/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.util.List;

import lombok.Data;


/**
 * représente le cache pour stocker les rzsultats aux Elp et Epreuves déjà récupérés pour l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class CacheResultatsElpEpr {

	/**
	 * Faux si c'est les résultats visibles pour l'enseignant
	 * Vrai si c'est à destination de l'étudiant.
	 */
	private boolean vueEtudiant;
	/**
	 * l'étape concernée par ces résultats.
	 */
	private Etape etape;
	/**
	 * la liste des éléments pédagogique (avec résultats) d'une étape choisie.
	 */
	private List<ElementPedagogique> elementsPedagogiques;
	


}
