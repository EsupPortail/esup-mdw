package fr.univlorraine.mondossierweb.beans;


import lombok.Data;

/**
 * classe qui représente les données d'affiliation à la sécu de l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class AffiliationSSO {
	
	private String dat_cotisation;
	private String dat_effet;
	private String cmp;
	private String etape;
	private String centre_payeur;
	private String mutuelle;

}
