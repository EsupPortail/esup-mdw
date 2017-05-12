package fr.univlorraine.mondossierweb.beans;


import lombok.Data;

/**
 * classe qui représente les données d'un droit universitaire payé par l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class DroitUniversitaire {

	private String lic_droit_paye;
	private String mnt_droit_paye;
}
