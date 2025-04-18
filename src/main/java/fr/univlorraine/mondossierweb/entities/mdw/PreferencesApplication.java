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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Entity
@Data
@Table(name="PREFERENCES_APPLICATION")
@EqualsAndHashCode(of = "prefId")
@ToString(exclude = {"preferencesApplicationValeurs"})
public class PreferencesApplication implements Serializable {

	@Id
	@Column(name="PREF_ID")
	private String prefId;
	
	@Column(name="PREF_DESC")
	private String prefDesc;
	
	@Column(name="VALEUR")
	private String valeur;
	
	@Column(name="TYPE")
	private String type;
	
	@Column(name="CAT_ID")
	private Integer catId;
	
	@OneToMany(mappedBy="preferencesApplication", cascade = {CascadeType.ALL}, orphanRemoval = true)
	private List<PreferencesApplicationValeurs> preferencesApplicationValeurs;

	public PreferencesApplicationValeurs addPreferencesApplicationValeur(PreferencesApplicationValeurs preferencesApplicationValeur) {
		getPreferencesApplicationValeurs().add(preferencesApplicationValeur);
		preferencesApplicationValeur.setPreferencesApplication(this);

		return preferencesApplicationValeur;
	}

	public PreferencesApplicationValeurs removePreferencesApplicationValeur(PreferencesApplicationValeurs preferencesApplicationValeur) {
		getPreferencesApplicationValeurs().remove(preferencesApplicationValeur);
		preferencesApplicationValeur.setPreferencesApplication(null);

		return preferencesApplicationValeur;
	}
}
