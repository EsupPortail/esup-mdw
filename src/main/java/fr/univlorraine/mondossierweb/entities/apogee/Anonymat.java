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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * représente un numéro d'anonymat
 * @author Charlie Dubois
 */
@Entity
@Data
public class Anonymat {
	
	/**
	 * id de l'anonymat.
	 */
	@Id
	@Column(name="ID")
	private int id;
	
	/**
	 * le numero d'anonymat.
	 */
	@Column(name="COD_ETU_ANO")
	private String cod_etu_ano;
	
	/**
	 * le libelle de la maquette d'anonymat.
	 */
	@Column(name="LIB_MAN")
	private String lib_man;
	

	
	
}
