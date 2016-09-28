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
package fr.univlorraine.mondossierweb.beans;

import java.util.List;

import lombok.Data;

/**
 * représente le cache pour stocker les résultats aux diplomes et étapes déjà récupérés pour l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class CacheResultatsVdiVet {
	
	/**
	 * Faux si c'est les résultats visibles pour l'enseignant
	 * Vrai si c'est à destination de l'étudiant.
	 */
	private boolean vueEtudiant;
	/**
	 * les diplomes avec les résultats obtenus.
	 */
	private List<Diplome> diplomes;
	/**
	 * les etapes avec les résultats obtenus.
	 */
	private List<Etape> etapes;
	
	

}
