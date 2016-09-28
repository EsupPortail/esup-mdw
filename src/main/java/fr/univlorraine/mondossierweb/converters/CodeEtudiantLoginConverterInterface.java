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
package fr.univlorraine.mondossierweb.converters;

/**
 * interface listant les méthodes devant être implémentées par la classe 
 * qui récupérera le cod_etu depuis le login Etudiant.
 * @author Charlie Dubois
 *
 */
public interface CodeEtudiantLoginConverterInterface {

	/**
	 * @param login
	 * @return le codetu d'un étudiant à partir de son login
	 */
	String getCodEtuFromLogin(String login);
	
	
}
