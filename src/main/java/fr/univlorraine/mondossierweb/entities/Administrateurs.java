package fr.univlorraine.mondossierweb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="ADMINISTRATEURS")
public class Administrateurs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2301719865870658196L;
	
	@Id
	private String login;


}
