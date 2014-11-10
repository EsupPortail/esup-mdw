/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.util.List;

import lombok.Data;

/**
 * représente le cache pour stocker les résultats aux diplomes et étapes déjà récupérés pour l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class CacheResultatsVdiVet {
	
	/**
	 * Faux si c'est les résultats visibles pour l'enseignant
	 * Vrai si c'est à destination de l'étudiant.
	 */
	private boolean vueEtudiant;
	/**
	 * les diplomes avec les résultats obtenus.
	 */
	private List<Diplome> diplomes;
	/**
	 * les etapes avec les résultats obtenus.
	 */
	private List<Etape> etapes;
	
	

}
