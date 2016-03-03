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
 * classe qui représente les adresses de l'étudiant
 * @author Charlie Dubois
 */
@Data
public class Adresse {

	/**
	 * annee pour une adresse annuelle
	 */
	private String annee;
	
	private String adresse1;
	
	private String adresse2;
	
	private String adresse3;
	
	private String adresseetranger;
	
	private String codePostal;
	
	private String ville;
	
	private String pays;
	
	private String codPays;
	
	private String numerotel;
	
	private String type;

	public Adresse() {
		super();
		
	}
	

	


}
