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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import fr.univlorraine.mondossierweb.utils.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
//@Configurable(preConstruction=true)
@Data
@Slf4j
public class MdwUserDetails implements UserDetails {

	private String username;
	
	private String codetu;

	private boolean admin;

	private boolean isEnseignant;

	private boolean isEtudiant;

	private String type;
	
	private String ip;


	@Getter
	private Collection<GrantedAuthority> authorities = new ArrayList<>();


	public MdwUserDetails(String username, String[] profil, boolean canAccessToAdminView, String ip) {

		log.info("Connexion-IP:"+ip+"-Login:"+username+"-Profil:"+profil[0]+(profil.length==2?"-"+profil[1]:"")+"-AdminView:"+canAccessToAdminView);

		this.username = username;
		String droit = profil[0];
		
		this.ip = ip;

		/* load Authorities */
		authorities.add(new SimpleGrantedAuthority(droit));

		//Si le user a le droit d'accéder à la vue admin
		if(canAccessToAdminView){
			authorities.add(new SimpleGrantedAuthority(MdwUserDetailsService.CONSULT_ADMINVIEW_AUTORISE));
		}

		this.type = droit;

		if(droit.equals(Utils.TEACHER_USER)){
			this.isEnseignant = true;
		}

		if(droit.equals(Utils.STUDENT_USER)){
			this.isEtudiant = true;
			this.codetu = profil[1];
		}

		//Si admin ou teacher ou student , le user est autorisé a consulter un dossier étudiant
		if(droit.equals(Utils.ADMIN_USER) || droit.equals(Utils.TEACHER_USER) || droit.equals(Utils.STUDENT_USER)){
			authorities.add(new SimpleGrantedAuthority(MdwUserDetailsService.CONSULT_DOSSIER_AUTORISE));
		}

		//Si admin
		if(droit.equals(Utils.ADMIN_USER)){
			this.admin = true;
			//on rajoute le droit teacher_user
			this.isEnseignant = true;
			authorities.add(new SimpleGrantedAuthority(Utils.TEACHER_USER));
		}


	}


	@Override
	public String getPassword() {
		return "";
	}


	@Override
	public String getUsername() {
		return username;
	}


	@Override
	public boolean isAccountNonExpired() {
		return true;
	}


	@Override
	public boolean isAccountNonLocked() {
		return true;
	}


	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}


	@Override
	public boolean isEnabled() {
		return true;
	}

}


