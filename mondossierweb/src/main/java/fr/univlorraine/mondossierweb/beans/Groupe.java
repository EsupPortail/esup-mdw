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

/**
 * Représente un groupe d'étudiants
 * @author chdubois
 *
 */
@Data
public class Groupe {

	private String cleGroupe;
	/**
	 * le code du groupe
	 */
	private String codGroupe;
	/**
	 * le libelle du groupe
	 */
	private String libGroupe;
	/**
	 * la capacite max du groupe
	 */
	private int capMaxGpe;
	/**
	 * la capacite intermédiaire du groupe
	 */
	private int capIntGpe;
	/**
	 * nombre d'inscrits dans le groupe
	 */
	private int nbInscrits;
	/**
	 * vrai si capInt superieure a zero, donc on l'affiche
	 */
	private boolean affCapIntGpe;
	
	public Groupe(String codGroupe,String libGroupe, int capMaxGpe, int capIntGpe) {
		super();
		this.capIntGpe = capIntGpe;
		this.capMaxGpe = capMaxGpe;
		this.codGroupe = codGroupe;
		this.libGroupe = libGroupe;
	}

	
	public Groupe(String codGroupe) {
		super();
		this.codGroupe = codGroupe;
	}


	
}
