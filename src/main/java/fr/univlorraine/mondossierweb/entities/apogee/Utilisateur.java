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

import fr.univlorraine.mondossierweb.entities.converters.BooleanToStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="UTILISATEUR")
@Data
public class Utilisateur {

	@Id
	@Column(name="COD_UTI")
	private String codUti;
	

	@Column(name="TEM_EN_SVE_UTI")
	@Convert(converter=BooleanToStringConverter.class)
	private boolean temEnService;

}
