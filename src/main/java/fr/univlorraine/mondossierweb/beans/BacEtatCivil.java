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
 * classe qui décrit le bac obtenu par l'étudiant.
 * @author Charlie Dubois
 */
@Data
public class BacEtatCivil {
	
	/**
	 * le libelle du bac.
	 */
	private String lib_bac;
	/**
	 * le code du bac.
	 */
	private String cod_bac;
	/**
	 * l'année d'obtention du bac.
	 */
	private String daa_obt_bac_iba;
	/**
	 * le code de la mention obtenue.
	 */
	private String cod_mnb;
	/**
	 * le code du type d'établissement.
	 */
	private String cod_tpe;
	/**
	 * le code de l'établissement.
	 */
	private String cod_etb;
	/**
	 * le code du département.
	 */
	private String cod_dep;
	

}
