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
import java.util.List;

import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import lombok.Data;

/**
 * représente une des infos sur une année universitaire
 * @author Charlie Dubois
 */
@Data
public class InfosAnnuelles implements Serializable {


	/**
	 * année univ concernée
	 */
	private String annee;
	/**
	 * vrai s'il s'agit de l'année en cours dans Apogée
	 */
	private boolean anneeEnCours;
	/**
	 * libellé
	 */
	private String libelle;
	/**
	 * la liste des numérots d'anonymat pour l'année en cours
	 */
	private List<Anonymat> numerosAnonymat;
	/**
	 * Le numéro de boursier
	 */
	private boolean boursier;
	/**
	 * vrai si étudiant est salarié
	 */
	private boolean temSalarie;
	/**
	 * vrai si étudiant a un régime d'aménagement d'étude
	 */
	private boolean temAmenagementEtude;
	

	/**
	 * constructeur vide.
	 *
	 */
	public InfosAnnuelles() {
		super();
	}
	
	
}
