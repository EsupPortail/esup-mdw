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

import lombok.Data;



/**
 * classe qui représente le résultat d'un étudiant.
 * @author Charlie Dubois
 */
@Data
public class Resultat implements Serializable{

	
	private static final long serialVersionUID = 4809562988462399839L;
		/**
		 * la session.
		 */
		private String session;
		/**
		 * la note.
		 */
		private String note;
		/**
		 * le bareme
		 */
		private int bareme;
		/**
		 * le résultat (admis ou pas).
		 */
		private String admission;
		/**
		 * le code de la mention
		 */
		private String codMention;
		/**
		 * le code de la mention
		 */
		private String libMention;
		
		

		
}
