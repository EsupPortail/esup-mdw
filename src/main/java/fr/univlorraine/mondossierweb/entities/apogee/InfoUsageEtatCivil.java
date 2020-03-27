package fr.univlorraine.mondossierweb.entities.apogee;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

/**
 * classe qui représente les infos d'usage de l'état-civil de l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Entity
/*@SqlResultSetMapping(name="InfoUsageEtatCivil",
entities={
		@EntityResult(entityClass=InfoUsageEtatCivil.class, fields={
			@FieldResult(name="temPrUsage", column="temPrUsage"),
			@FieldResult(name="codSexEtatCiv", column="codSexEtatCiv"),
			@FieldResult(name="libPrEtaCiv", column="libPrEtaCiv")
		})
})*/
@Data
public class InfoUsageEtatCivil {

	@Id
	@Column(name="codInd")
	private String codInd;
	
	@Column(name="codCiv")
	private String codCiv;
	
	@Column(name="temPrUsage")
	private boolean temPrUsage; 
	
	@Column(name="codSexEtatCiv")
	private String codSexEtatCiv;
	
	@Column(name="libPrEtaCiv")
	private String libPrEtaCiv;
}
