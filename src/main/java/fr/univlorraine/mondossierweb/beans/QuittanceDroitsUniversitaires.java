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
package fr.univlorraine.mondossierweb.beans;


import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * classe qui représente les données de quittance des droits universitaires payés de l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class QuittanceDroitsUniversitaires {
	
	private String dat_quittance1;
	private String num_quittance1;
	private String lic_cge_quittance1;
	private String lic_mdp1_quittance1;
	private String lic_mdp2_quittance1;
	private String dat_quittance2;
	private String num_quittance2;
	private String lic_cge_quittance2;
	private String lic_mdp1_quittance2;
	private String lic_mdp2_quittance2;
	private boolean pmt_3x;
	private String mnt_pmt1;
	private String dat_pmt1;
	private String mnt_pmt2;
	private String dat_pmt2;
	private String mnt_pmt3;
	private String dat_pmt3;
	private List<DroitUniversitaire> list_droits_payes;
	private String mnt_total;
	
	public void initValues(){
		dat_quittance1 = "";
		num_quittance1 = "";
		
		dat_quittance2 = "";
		num_quittance2 = "";
	}

}
