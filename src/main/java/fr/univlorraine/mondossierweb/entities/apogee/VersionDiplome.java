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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.FieldResult;
import jakarta.persistence.SqlResultSetMapping;
import lombok.Data;

@Entity
@SqlResultSetMapping(name="VersionDiplome",
entities={
		@EntityResult(entityClass=VersionDiplome.class, fields={
			@FieldResult(name="id.cod_dip", column="cod_dip"),
			@FieldResult(name="id.cod_vrs_vdi", column="cod_vrs_vdi"),
			@FieldResult(name="lib_web_vdi", column="lib_web_vdi"),
			@FieldResult(name="cod_tpd_etb", column="cod_tpd_etb")
		})
})
@Data
public class VersionDiplome {

	
	@EmbeddedId
	private VersionDiplomePK id;
	
	private String lib_web_vdi;
	
	private String cod_tpd_etb;


}
