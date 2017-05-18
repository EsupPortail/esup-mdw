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
package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;
import java.util.Map;

public interface SsoApogeeService {

	public String getMutuelle(String codAnu, String codInd);
	
	public List<Map<String,String>> getQuittances(String codAnu, String codInd);
	
	public Map<String,String> getCentrePayeur(String codAnu, String codInd, boolean affilie);
	
	public String getDateCotisation(String codAnu, String codInd);
	
	//public boolean isAffilieSso(String codAnu, String codInd);
	
	public List<String> getMoyensDePaiement(String codAnu,String codInd, String NumOccSqr);

	public boolean isPaiement3X(String codAnu, String codInd);

	public String getDate1erPaiement(String codAnu, String codInd);

	public String getDate2emPaiement(String codAnu, String codInd);

	public String getDate3emPaiement(String codAnu, String codInd);

	public String getMontant1erPaiement(String codAnu, String codInd);

	public String getMontant2emPaiement(String codAnu, String codInd);

	public String getMontant3emPaiement(String codAnu, String codInd);

	public String getMontantTotalPaye(String codAnu, String codInd);

	public List<Map<String, String>> getMontantsPayes(String codAnu, String codInd);

}
