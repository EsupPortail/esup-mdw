/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;



public interface InscriptionService {

	public String getProfil(String codAnu, String codInd);
	
	public String getCgeFromCodIndIAE(String codAnu, String codInd, String codEtp, String vrsVet);
	
	public String getCmpFromCodIndIAE(String codAnu, String codInd, String codEtp, String vrsVet);

	public String getFormationEnCours(String codetu);
}
