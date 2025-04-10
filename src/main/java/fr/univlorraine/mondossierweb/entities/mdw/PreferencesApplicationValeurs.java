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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Data
@Table(name="PREFERENCES_APPLICATION_VALEURS")
//@EqualsAndHashCode(of = {"pavId", "idInCaseOfNeed"})
@EqualsAndHashCode(of = {"pavId", "preferencesApplication","valeur"})
@ToString(exclude = {"preferencesApplication"})
public class PreferencesApplicationValeurs implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="PAV_ID")
	private Integer pavId;

/*  @Transient
	private Long idInCaseOfNeed = System.nanoTime();*/

	@Column(name = "VALEUR")
	private String valeur;


	@ManyToOne
	@JoinColumn(name="PREF_ID")
	private PreferencesApplication preferencesApplication;



}
