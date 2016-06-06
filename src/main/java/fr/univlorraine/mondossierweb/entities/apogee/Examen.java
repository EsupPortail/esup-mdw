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
package fr.univlorraine.mondossierweb.entities.apogee;



import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

/**
 * classe qui représente un examen du calendrier.
 * @author Charlie Dubois
 */
@Entity
@Data
public class Examen {

	@Id
	@Column(name = "ID")
	private int id;

	/**
	 * date de l'événement.
	 */
	@Column(name = "datedeb")
	private Date datedeb;
	/**
	 * heure de l'événement.
	 */
	@Column(name = "heure")
	private String heure;
	/**
	 * duree de l'événement.
	 */
	@Column(name = "duree")
	private String duree;
	/**
	 * salle ou se déroule l'événement.
	 */
	@Column(name = "salle")
	private String salle;
	/**
	 * libellé de la salle ou se déroule l'événement.
	 */
	@Column(name = "libsalle")
	private String libsalle;
	/**
	 * place dans la salle.
	 */
	@Column(name = "place")
	private String place;
	/**
	 * batiment ou se déroule l'événement.
	 */
	@Column(name = "batiment")
	private String batiment;
	/**
	 * localisation du batiment ou se déroule l'événement.
	 */
	@Column(name = "localisation")
	private String localisation;
	/**
	 * épreuve concernéé par l'événement.
	 */
	@Column(name = "epreuve")
	private String epreuve;
	/**
	 * centre incompatibilité concernév par l'événement.
	 */
	@Column(name = "codcin")
	private String codcin;
	/**
     * code de l'épreuve
     */
    @Column(name = "codeepreuve")
    private String codeepreuve;
    /**
     * libellé session
     */
    @Column(name = "libsession")
    private String libsession;
    /**
     * code de l'étape
     */
    @Column(name = "codeetape")
    private String codeetape;
    /**
     * version de l'étape
     */
    @Column(name = "versionetape")
    private String versionetape;
	
}
