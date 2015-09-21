/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.mdw;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class FavorisPK implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7240432999800071780L;

	@Column(name="LOGIN")
	private String login;

	@Column(name="TYP_FAV")
	private String typfav;
	
	@Column(name="ID_FAV")
	private String idfav;


}
