/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.mdw;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name="FAVORIS")
@NamedQuery(name="Favoris.findAll", query="SELECT f FROM Favoris f")
@EqualsAndHashCode(of="id")
public class Favoris implements Serializable {


	private static final long serialVersionUID = -1983771824782315564L;
	
	@EmbeddedId
	private FavorisPK id;


}
