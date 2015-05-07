package fr.univlorraine.mondossierweb.entities.mdw;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="PREFERENCES_APPLICATION")
public class PreferencesApplication implements Serializable {

	private static final long serialVersionUID = 5299213936744275485L;

	@Id
	@Column(name="PREF_ID")
	private String prefId;
	
	@Column(name="PREF_DESC")
	private String prefDesc;
	
	@Column(name="VALEUR")
	private String valeur;

}
