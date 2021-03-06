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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import lombok.Data;

@Entity
@SqlResultSetMapping(name="ElementPedagogique",
entities={
		@EntityResult(entityClass=ElementPedagogique.class, fields={
			@FieldResult(name="cod_elp", column="cod_elp"),
			@FieldResult(name="lib_elp", column="lib_elp")
		})
})
@Table(name="ELEMENT_PEDAGOGI")
@Data
public class ElementPedagogique implements Serializable {
	
	@Id
	private String cod_elp;

	private String lib_elp;
	
	

	
}
