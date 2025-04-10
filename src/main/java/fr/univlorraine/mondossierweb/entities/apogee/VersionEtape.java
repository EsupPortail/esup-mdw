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
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@SqlResultSetMapping(name="VersionEtape",
entities={
		@EntityResult(entityClass=VersionEtape.class, fields={
			@FieldResult(name="id.cod_etp", column="cod_etp"),
			@FieldResult(name="id.cod_vrs_vet", column="cod_vrs_vet"),
			@FieldResult(name="lib_web_vet", column="lib_web_vet")
		})
})
@Table(name="VERSION_ETAPE")
@Data
public class VersionEtape {

	
	@EmbeddedId
	private VersionEtapePK id;
	
	private String lib_web_vet;


}
