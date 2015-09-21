/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.mdw;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;





import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
@Table(name="UTILISATEUR_SWAP")
public class UtilisateurSwap implements Serializable {

	private static final long serialVersionUID = 6093580014334823119L;

	@Id
	@Column(name="LOGIN_SOURCE")
	private String loginSource;
	
	@Column(name="LOGIN_CIBLE")
	private String loginCible;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DAT_CRE")
	private Date datCre;

}
