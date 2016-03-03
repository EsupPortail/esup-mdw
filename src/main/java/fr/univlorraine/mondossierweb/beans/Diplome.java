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
 * Diplôme
 * @author Charlie Dubois
 */
@Data
public class Diplome implements Serializable{

	private static final long serialVersionUID = -725836072091894826L;
	/**
	 * code de la composante a laquelle appartient le diplome.
	 */
	private String cod_cmp;
	/**
	 * libelle de la composante a laquelle appartient le dipl�me.
	 */
	private String lib_cmp;
	/**
	 * code du diplôme
	 */
	private String cod_dip;
	/**
	 * code type diplôme  établissement.
	 */
	private String cod_tpd_etb;
	/**
	 * version du diplôme.
	 */
	private String cod_vrs_vdi;
	/**
	 * libelle du diplôme.
	 */
	private String lib_web_vdi;
	/**
	 * année universitaire d'obtention.
	 */
	private String annee;
	/**
	 * le rang de l'étudiant pour son résultat au diplome.
	 */
	private String rang;
	/**
	 * vrai si on doit afficher le rang de l'étudiant à ce diplome.
	 */
	private boolean afficherRang;
	/**
	 * les résultats.
	 */
	private List<Resultat> resultats;
	
	/**
	 * constructeur vide.
	 *
	 */
	public Diplome() {
		super();
		resultats = new ArrayList<Resultat>();
	}
	
	
}
