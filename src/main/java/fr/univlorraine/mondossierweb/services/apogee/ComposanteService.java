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
package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;

import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.VersionDiplome;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;

public interface ComposanteService {
	
	public List<Composante> findComposantesEnService();
	
	public String getLibelleComposante(String codCmp);
	
	public List<VersionDiplome> findVdiFromComposante(String annee, String cod_cmp);
	
	public List<VersionEtape> findVetFromVdiAndCmp(String annee,String cod_vdi, String vrs_vdi, String codcmp);
	
	public List<ElementPedagogique> findElpFromVet(String codEtp, String vrsEtp);
	
	public List<ElementPedagogique> findElpFromElp(String codElp);
}
