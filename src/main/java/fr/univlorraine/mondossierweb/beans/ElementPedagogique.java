/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.beans;

import lombok.Data;

import java.io.Serializable;

/**
 * représente un élément pédagogique.
 * @author Charlie Dubois
 */
@Data
public class ElementPedagogique implements Serializable {

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
	 * la mention obtenue
	 */
	private String codMention;

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
