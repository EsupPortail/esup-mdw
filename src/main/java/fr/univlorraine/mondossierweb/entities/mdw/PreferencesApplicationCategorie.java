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
package fr.univlorraine.mondossierweb.entities.mdw;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Entity
@Data
@Table(name="PREFERENCES_APPLICATION_CATEGORIE")
public class PreferencesApplicationCategorie implements Serializable {

	@Id
	@Column(name="CAT_ID")
	private Integer catId;
	
	@Column(name="CAT_DESC")
	private String catDesc;
	
	@Column(name="ORDRE")
	private Integer ordre;
	
	// bi-directional many-to-one association to Facture
	/*@OneToMany(mappedBy = "categorie", fetch = FetchType.EAGER)
	private List<PreferencesApplication> preferencesApplication;*/
	

	

}
