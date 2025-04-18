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
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;


/**
 * The persistent class for the DIPLOME database table.
 * 
 */
@Entity
@Table(name="DIPLOME")
@Data
public class DiplomeApogee implements Serializable {
	@Id
	@Column(name="COD_DIP")
	private String codDip;

	@Column(name="LIB_DIP")
	private String libDip;

	
	@Column(name="COD_TPD_ETB")
	private String codTpdEtb;


	

}
