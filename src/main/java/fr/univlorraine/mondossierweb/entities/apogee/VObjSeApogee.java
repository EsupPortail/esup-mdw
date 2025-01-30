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
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;


/**
 * The persistent class for the ETAPE database table.
 * 
 */
@Entity
@Table(name="V_OBJ_SE")
@Data
public class VObjSeApogee implements Serializable {
	private static final long serialVersionUID = 1L;

	
	@EmbeddedId
	private VObjSeApogeePK id;
	
	
	/*@Id
	@Column(name="COD_OBJ")
	private String codObj;
	
	@Column(name="COD_VRS_OBJ")
	private int codVrsObj;

	@Column(name="TYP_OBJ")
	private String typObj;*/

	@Column(name="LIB_OBJ")
	private String libObj;

	

}
