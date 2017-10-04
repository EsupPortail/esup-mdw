package fr.univlorraine.mondossierweb.beans;

import lombok.Data;

/**
 * classe qui représente le couple de libelle cmp/etape.
 * @author Charlie Dubois
 */
@Data
public class LibCmpEtape {



	private String lib_cmp;
	
	private String lib_etape;
	
	public LibCmpEtape(String cmp, String etape) {
		this.lib_cmp = cmp;
		this.lib_etape = etape;
	}
}
