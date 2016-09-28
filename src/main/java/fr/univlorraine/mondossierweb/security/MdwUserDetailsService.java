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
package fr.univlorraine.mondossierweb.security;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.controllers.UserController;



@Component
public class MdwUserDetailsService implements UserDetailsService {


	/**
	 * type utilisateur admin.
	 */
	public static final String ADMIN_USER = "admin";

	public static final String CONSULT_DOSSIER_AUTORISE = "consultation_dossier";
	
	public static final String CONSULT_ADMINVIEW_AUTORISE = "consultation_adminView";

	@Resource
	private transient UserController userController;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		//Au cas où on ait un swap d'utilisateur
		String finalusername = userController.getCurrentUserName(username);


		//Si le login utilisé est admin
		if(userController.isAdmin(finalusername)){
			return new MdwUserDetails(finalusername,ADMIN_USER, true);
		}

		//Si il y un swap et que le login d'origne est admin
		if(!finalusername.equals(username) && userController.isAdmin(username)){
			//On doit donner acces à la vue admin
			return new MdwUserDetails(finalusername,userController.determineTypeUser(finalusername), true);
		}

		return new MdwUserDetails(finalusername,userController.determineTypeUser(finalusername), false);
	}


}
