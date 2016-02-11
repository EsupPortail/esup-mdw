/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.util.List;

import lombok.Data;

/**
 * représente une collection de groupes d'�tudiants
 * @author chdubois
 *
 */
@Data
public class CollectionDeGroupes {

	/**
	 * le code de la collection
	 */
	private String codCollection;
	/**
	 * la liste des groupes de la collection
	 */
	private List<Groupe> listeGroupes;


	public CollectionDeGroupes(String CodCollection) {
		super();
		codCollection = CodCollection;
	}

	

	
	
	
}
