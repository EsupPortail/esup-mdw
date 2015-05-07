package fr.univlorraine.mondossierweb.entities.mdw;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name="preferences_utilisateur")
@EqualsAndHashCode(of="id")
public class PreferencesUtilisateur implements Serializable {
	
	private static final long serialVersionUID = 4738595766321367947L;

	@EmbeddedId
	private PreferencesUtilisateurPK id;

	@Column(name="VALEUR")
	private String valeur;

}
