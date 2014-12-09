package fr.univlorraine.mondossierweb.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class PreferencesUtilisateurPK implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4178790093683625317L;

	@Column(name="LOGIN")
	private String login;

	@Column(name="PREF_ID")
	private String prefid;
	


}
