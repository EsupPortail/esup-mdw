/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

/**
 * représente le cache pour stocker les résultats déjà récupérés pour l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class CacheResultats {

	/**
	 * liste des résultats possible pour la page 'Notes' déjà récupérés (2 possibles : vue Enseignant/vueEtudiant)
	 */
	private List<CacheResultatsVdiVet>  ResultVdiVet;
	/**
	 * liste des résultats possible pour la page 'DétailsDesNotes' déjà récupérés.Fonction de la vue et de l'étape observée.
	 */
	private List<CacheResultatsElpEpr> ResultElpEpr;
	
	public CacheResultats(){
		super();
		ResultVdiVet = new LinkedList<CacheResultatsVdiVet>();
		ResultElpEpr = new LinkedList<CacheResultatsElpEpr>();
	}
	
	
	
}
