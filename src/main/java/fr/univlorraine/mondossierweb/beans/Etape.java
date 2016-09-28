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
	 * année de l'étape.
	 */
	private String annee;
	/**
	 * libellé de l'étape.
	 */
	private String libelle;
	/**
	 * code du diplôme père.
	 */
	private String cod_dip;
	/**
	 * version du diplome père;
	 */
	private int vers_dip;
	/**
	 * le rang de l'étudiant pour son résultat a l'étape.
	 */
	private String rang;
	/**
	 * les résultats.
	 */
	private List<Resultat> resultats;
	/**
	 * vrai si les résultat à l'épreuve sont définitifs.
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
