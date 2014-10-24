package fr.univlorraine.mondossierweb.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import fr.univlorraine.mondossierweb.entities.listeners.EntityPushEntityListener;

@Entity @EntityListeners(EntityPushEntityListener.class)
@Data @EqualsAndHashCode(of="codStr")
@Table(name="STRUCTURE")
public class Structure {

	@Id
	@Column(name="COD_STR", length=5, nullable = false)
	@Size(min=1, max=5) @NotNull
	private String codStr;

	@Column(name="UAI", length=10)
	@Size(max=10)
	private String uai;

	@Column(name="COD_APO", length=3)
	@Size(max=3)
	private String codApo;

	@Column(name="COD_STR_MER", length=5)
	@Size(max=5)
	private String codStrMer;

	@Column(name="LIB_CRT", length=25)
	@Size(max=25)
	private String libCrt;

	@Column(name="LIB_JUR", length=160)
	@Size(max=160)
	private String libJur;

	@Column(name="LIB_LNG", length=40)
	@Size(max=40)
	private String libLng;

	@Column(name="LISTE_NOM_DNS", length=150)
	@Size(max=150)
	private String listeNomDns;

	@Column(name="NOM_SYMB_FILER", length=100)
	@Size(max=100)
	private String nomSymbFiler;

	@Column(name="PTI_NOM", length=25)
	@Size(max=25)
	private String ptiNom;

	@Column(name="PTI_NOM_VALIDE", length=3)
	@Size(max=3)
	private String ptiNomValide;

	@Column(name="TYP_STR", length=20)
	@Size(max=20)
	private String typStr;

}
