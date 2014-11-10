/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * représente une étape
 * @author Charlie Dubois
 */
@Data
public class Etape implements Serializable {

	private static final long serialVersionUID = -4920377897525522629L;
	/**
	 * code de l'étape.
	 */
	private String code;
	/**
	 * version de l'étape.
	 */
	private String version;
	/**
	 * ann�e de l'étape.
	 */
	private String annee;
	/**
	 * libell� de l'étape.
	 */
	private String libelle;
	/**
	 * code du dipl�me p�re.
	 */
	private String cod_dip;
	/**
	 * version du diplome p�re;
	 */
	private int vers_dip;
	/**
	 * le rang de l'étudiant pour son r�sultat a l'étape.
	 */
	private String rang;
	/**
	 * les r�sultats.
	 */
	private List<Resultat> resultats;
	/**
	 * vrai si les r�sultat � l'�preuve sont d�finitifs.
	 */
	private boolean deliberationTerminee;
	/**
	 * vrai si on doit afficher le rang de l'étudiant à l'étape.
	 */
	private boolean afficherRang;
	
	/**
	 * constructeur.
	 * @param code
	 * @param version
	 * @param annee
	 */
	public Etape(String code, String version, String annee) {
		super();
		this.code = code;
		this.version = version;
		this.annee = annee;
		resultats = new ArrayList<Resultat>();
		deliberationTerminee = false;
	}
	/**
	 * constructeur vide.
	 *
	 */
	public Etape() {
		super();
		resultats = new ArrayList<Resultat>();
	}
	
	
}
