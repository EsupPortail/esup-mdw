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
package fr.univlorraine.mondossierweb.utils;

import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Charlie Dubois
 * 
 * Accès aux porperties du fichier de config des requetes sql
 */
@Component
@Data
public class RequestUtils {


	private Properties properties;

	public RequestUtils(){
		try {
			
			File file = new ClassPathResource("apogeeRequest.xml").getFile();
			FileInputStream fileInput = new FileInputStream(file);
			properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getCalendrierDesExamens(){
		return properties.getProperty("calendrierExamen");
	}
	
	public String getQuittances(){
		return properties.getProperty("quittances");
	}
	public String getCentrePayeurPourAffilie(){
		return properties.getProperty("centrePayeurPourAffilie");
	}
	
	public String getCodPcsSalarie(){
		return properties.getProperty("codPcsSalarie");
	}
	
	public String getMontant1erPaiement() {
		return properties.getProperty("montant1erPaiement");
	}
	
	public String getMontant2emPaiement() {
		return properties.getProperty("montant2emPaiement");
	}
	
	public String getMontantsPayes(){
		return properties.getProperty("montantsPayes");
	}
	
	public String getMontantTotalPaye(){
		return properties.getProperty("montantTotalPaye");
	}
	
	public String getMontant3emPaiement() {
		return properties.getProperty("montant3emPaiement");
	}
	
	public String getDate1erPaiement(){
		return properties.getProperty("date1erPaiement");
	}
	
	public String getDate2emPaiement(){
		return properties.getProperty("date2emPaiement");
	}
	
	public String getDate3emPaiement(){
		return properties.getProperty("date3emPaiement");
	}
	
	public String isPaiement3X(){
		return properties.getProperty("paiement3X");
	}
	
	public String getMoyensDePaiement(){
		return properties.getProperty("moyensDePaiement");
	}
	
	public String getDateCotisation(){
		return properties.getProperty("dateCotisation");
	}
	
	public String getCentrePayeurPourNonAffilie(){
		return properties.getProperty("centrePayeurPourNonAffilie");
	}
	
	public String getMutuelle(){
		return properties.getProperty("mutuelle");
	}
	
	public String getMailFromLogin() {
		return properties.getProperty("mailFromLogin");
	}


	public String getLoginFromCodEtu() {
		return properties.getProperty("loginFromCodEtu");
	}


	public String getMailFromCodEtu() {
		return properties.getProperty("mailFromCodEtu");
	}


	public String getTemBoursierIaa() {
		return properties.getProperty("temBoursierIaa");
	}


	public String getInscritsFromElp() {
		return properties.getProperty("inscritsFromElp");
	}

	public String getCodIndInscritsFromGroupe() {
		return properties.getProperty("codIndInscritsFromGroupe");
	}


	public String getTemSesUniVet() {
		return properties.getProperty("temSesUniVet");
	}

	public String getInscritsEtapeJuinSep() { return properties.getProperty("inscritsEtapeJuinSep"); }
	

}
