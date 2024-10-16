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

import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationValeurs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferencesApplicationValeursRepository extends JpaRepository<PreferencesApplicationValeurs, Integer> {
	@Query(value="SELECT v " +
			"FROM PreferencesApplicationValeurs v " +
			"WHERE v.preferencesApplication.prefId = ?1")
	public List<PreferencesApplicationValeurs> findValeursFromPrefId(String prefid);
}
