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
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Représente une nature d'ELP.
 * 
 * @author Charlie Dubois
 */
@Entity
@Table(name="NATURE_ELP")
@Data
public class NatureElp {

	/**
	 * Code nature elp.
	 */
	@Id
	@Column(name = "COD_NEL")
	private String cod_nel;

	/**
	 * libelle long.
	 */
	@Column(name = "LIB_NEL")
	private String lib_nel;

	/**
	 * libelle court.
	 */
	@Column(name = "LIC_NEL")
	private String lic_nel;

	/**
	 * témoin en service.
	 */
	@Column(name = "TEM_EN_SVE_NEL")
	private String tem_en_sve_nel;
	
}
