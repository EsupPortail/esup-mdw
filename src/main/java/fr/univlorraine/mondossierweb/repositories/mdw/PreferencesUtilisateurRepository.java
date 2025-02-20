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
package fr.univlorraine.mondossierweb.repositories.mdw;

import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateur;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateurPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferencesUtilisateurRepository extends JpaRepository<PreferencesUtilisateur, PreferencesUtilisateurPK> {

	
	
	@Query(value="SELECT p " +
			"FROM PreferencesUtilisateur p " +
			"WHERE p.id.login = ?1 "+
			"AND p.id.prefid = ?2")
	public PreferencesUtilisateur findOnePrefFromLoginAndPrefid(String login, String prefid);
}
