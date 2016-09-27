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
 * représente une inscription.
 * @author Charlie Dubois
 */
@Data
public class Inscription {

	/**
	 * année de l'inscription.
	 */
	private String daa_ent_etb;
	/**
	 * libellé de l'établissement de l'inscription.
	 */
	private String lib_etb;
	/**
	 * code année.
	 */
	private String cod_anu;
	/**
	 * code étape.
	 */
	private String cod_etp;
	/**
	 * libellé de l'étape.
	 */
	private String lib_etp;
	/**
	 * code de la composante
	 */
	private String cod_comp;
	/**
	 * libellé de la composante
	 */
	private String lib_comp;
	/**
	 * code du diplome
	 */
	private String cod_dip;
	/**
	 * version du diplome
	 */
	private String vers_dip;
	/**
	 * libelle du diplome
	 */
	private String lib_dip;
	/**
	 * version de l'étape.
	 */
	private String cod_vrs_vet;
	
	/**
	 * vrai si inscription en regle (payée)
	 */
	private boolean estEnRegle;
	/**
	 * code individu
	 */
	private String cod_ind;
	/**
	 * code dac.
	 */
	private String cod_dac;
	/**
	 * libelle composante.
	 */
	private String lib_cmt_dac;
	/**
	 * résultat.
	 */
	private String res;
	
	
	
	
}
