/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.io.Serializable;

import lombok.Data;

/**
 * représente un élément pédagogique.
 * @author Charlie Dubois
 */
@Data
public class ElementPedagogique implements Serializable {
	
	private static final long serialVersionUID = 5197935378325878240L;
	/**
	 * code de l'élément pédagogique.
	 */
	private String code;
	/**
	 * année de l'élément pédagogique.
	 */
	private String annee;
	/**
	 * libellé de l'élément pédagogique.
	 */
	private String libelle;
	/**
	 * vrai si l'elp est une epreuve
	 */
	private boolean isEpreuve;
	/**
	 * note session de juin.
	 */
	private String note1;
	/**
	 * le barement pour la note1
	 */
	private int bareme1;
	/**
	 * r�sultat session de juin.
	 */
	private String res1;
	/**
	 * note session de septembre.
	 */
	private String note2;
	/**
	 * Le bareme pour la note2
	 */
	private int bareme2;
	/**
	 * r�sultat session de septembre.
	 */
	private String res2;
	/**
	 * le rang de l'étudiant pour son résultat à l'elp.
	 */
	private String rang;
	/**
	 * ects saisi dans la structure des enseignements.
	 */
	private String ects;
	/**
	 * le témoin fictif
	 */
	private String temFictif;
	/**
	 * niveau dans l'arborescence.
	 */
	private int level;
	/**
	 * elp supérieur (père).
	 */
	private String codElpSup;
	/**
	 * ajout Bordeaux1
	 * le témoin semestre
	 */
	private String temSemestre;
	/**
	 * ajout Bordeaux1
	 * l'état de délibération
	 */
	private String etatDelib;
	

	/**
	 * constructeur.
	 * @param code
	 * @param annee
	 */
	public ElementPedagogique(final String code, final String annee) {
		super();
		this.code = code;
		this.annee = annee;
		note1 = "";
		res1 = "";
		note2 = "";
		res2 = "";
		level = 0;
		codElpSup = "";
	}
	/**
	 * constructeur vide.
	 *
	 */
	public ElementPedagogique() {
		super();
		note1 = "";
		res1 = "";
		note2 = "";
		res2 = "";
		level = 0;
		codElpSup = "";
	}
	
}
