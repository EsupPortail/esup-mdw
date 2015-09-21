/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.util.List;

import lombok.Data;

/**
 * représente un ELP contenant des collections de groupes
 * @author chdubois
 *
 */
@Data
public class ElpDeCollection {
	/**
	 * le codeELP
	 */
	private String codElp;
	/**
	 * le libellé de l'ELP
	 */
	private String libElp;
	/**
	 * la liste de collection de l'ELP
	 */
	private List<CollectionDeGroupes> listeCollection;
	
	


	public ElpDeCollection(String CodElp, String LibElp) {
		super();
		codElp = CodElp;
		libElp = LibElp;
	}


	
	
	
	
}
