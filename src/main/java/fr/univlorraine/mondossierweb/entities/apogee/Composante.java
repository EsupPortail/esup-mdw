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

@Entity
@Table(name="COMPOSANTE")
@Data
public class Composante {

	@Id
	@Column(name="COD_CMP")
	private String codCmp;
	
	@Column(name="LIB_CMP")
	private String libCmp;

	@Column(name="LIC_CMP")
	private String licCmp;
	
	@Column(name="LIB_CMT_CMP")
	private String libCmtCmp;
	
	@Column(name="tem_en_sve_cmp")
	private String temEnSveCmp;


}
