/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import lombok.Data;

/**
 * classe qui décrit le bac obtenu par l'étudiant.
 * @author Charlie Dubois
 */
@Data
public class BacEtatCivil {
	
	/**
	 * le libelle du bac.
	 */
	private String lib_bac;
	/**
	 * le code du bac.
	 */
	private String cod_bac;
	/**
	 * l'année d'obtention du bac.
	 */
	private String daa_obt_bac_iba;
	/**
	 * le code de la mention obtenue.
	 */
	private String cod_mnb;
	/**
	 * le code du type d'établissement.
	 */
	private String cod_tpe;
	/**
	 * le code de l'établissement.
	 */
	private String cod_etb;
	/**
	 * le code du département.
	 */
	private String cod_dep;
	

}
