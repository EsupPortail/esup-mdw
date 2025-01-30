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
import lombok.Data;

/**
 * classe qui représente les infos d'usage de l'état-civil de l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Entity
/*@SqlResultSetMapping(name="InfoUsageEtatCivil",
entities={
		@EntityResult(entityClass=InfoUsageEtatCivil.class, fields={
			@FieldResult(name="temPrUsage", column="temPrUsage"),
			@FieldResult(name="codSexEtatCiv", column="codSexEtatCiv"),
			@FieldResult(name="libPrEtaCiv", column="libPrEtaCiv")
		})
})*/
@Data
public class InfoUsageEtatCivil {

	@Id
	@Column(name="codInd")
	private String codInd;
	
	@Column(name="codCiv")
	private String codCiv;
	
	@Column(name="temPrUsage")
	private boolean temPrUsage; 
	
	@Column(name="codSexEtatCiv")
	private String codSexEtatCiv;
	
	@Column(name="libPrEtaCiv")
	private String libPrEtaCiv;
}
