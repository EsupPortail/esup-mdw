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

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

/**
 * représente le cache pour stocker les résultats déjà récupérés pour l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class CacheResultats {

	/**
	 * liste des résultats possible pour la page 'Notes' déjà récupérés (2 possibles : vue Enseignant/vueEtudiant)
	 */
	private List<CacheResultatsVdiVet>  ResultVdiVet;
	/**
	 * liste des résultats possible pour la page 'DétailsDesNotes' déjà récupérés.Fonction de la vue et de l'étape observée.
	 */
	private List<CacheResultatsElpEpr> ResultElpEpr;
	
	public CacheResultats(){
		super();
		ResultVdiVet = new LinkedList<CacheResultatsVdiVet>();
		ResultElpEpr = new LinkedList<CacheResultatsElpEpr>();
	}
	
	
	
}
