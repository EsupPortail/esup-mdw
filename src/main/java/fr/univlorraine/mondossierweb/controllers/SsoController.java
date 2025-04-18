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
package fr.univlorraine.mondossierweb.controllers;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.AffiliationSSO;
import fr.univlorraine.mondossierweb.beans.DroitUniversitaire;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.beans.LibCmpEtape;
import fr.univlorraine.mondossierweb.beans.QuittanceDroitsUniversitaires;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;
import fr.univlorraine.mondossierweb.services.apogee.InscriptionService;
import fr.univlorraine.mondossierweb.services.apogee.InscriptionServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.services.apogee.SsoApogeeService;
import fr.univlorraine.mondossierweb.utils.PdfUtils;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gestion des infos de sécu (affiliation et quittance des droits)
 */
@Component
@Slf4j
public class SsoController {

	/**
	 * outputstream size.
	 */
	private static final int OUTPUTSTREAM_SIZE = 1024;
	/**
	 * marges.
	 */
	private static final float MARGIN_TOP = 5.0f;
	private static final float MARGIN_RIGHT = 2.0f;
	private static final float MARGIN_BOTTOM = 4.0f;
	private static final float MARGIN_LEFT = 3.0f;

	/* Injections */
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private SsoApogeeService ssoApogeeService;
	/** {@link InscriptionServiceImpl} */
	@Resource
	private InscriptionService inscriptionService;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private MultipleApogeeService multipleApogeeService;



	/**
	 * va chercher et renseigne les informations concernant l'affilication à la sécu
	 */
	public boolean recupererInfoQuittance(String codAnu,Etudiant e){
		log.debug("-recupererQuittance pour codetu "+e.getCod_etu());
		QuittanceDroitsUniversitaires q = new QuittanceDroitsUniversitaires();
		q.initValues();
		// Récupérer les quittances
		List<Map<String,String>> r = ssoApogeeService.getQuittances(codAnu, e.getCod_ind());
		if(r!=null && !r.isEmpty()){
			log.debug("nb quittances : "+r.size());
			if(r.get(0)!=null ){
				Map<String,String> m1 = r.get(0);
				log.debug("-quittance1 : "+m1);
				if(m1.get("NUMOCCQUT")!=null){
					q.setNum_quittance1(m1.get("NUMOCCQUT"));
				}
				if(m1.get("LICCGE")!=null){
					q.setLic_cge_quittance1(m1.get("LICCGE"));
				}
				if(m1.get("DATSQR")!=null){
					q.setDat_quittance1(m1.get("DATSQR"));
				}

				List<String> mdps=ssoApogeeService.getMoyensDePaiement(codAnu,e.getCod_ind(), m1.get("NUMOCCSQR"));
				if (mdps!=null && !mdps.isEmpty() && StringUtils.hasText(mdps.get(0))) {
					q.setLic_mdp1_quittance1(mdps.get(0));
					if (mdps.size() > 1 && StringUtils.hasText(mdps.get(1))) {
						q.setLic_mdp2_quittance1(mdps.get(1));
					}
				}

			}
			if (r.size() > 1 && r.get(1) != null ) {
				Map<String,String> m2 = r.get(1);
				log.debug("-quittance2 : "+m2);
				if(m2.get("NUMOCCQUT")!=null){
					q.setNum_quittance2(m2.get("NUMOCCQUT"));
				}
				if(m2.get("LICCGE")!=null){
					q.setLic_cge_quittance2(m2.get("LICCGE"));
				}
				if(m2.get("DATSQR")!=null){
					q.setDat_quittance2(m2.get("DATSQR"));
				}
				List<String> mdps=ssoApogeeService.getMoyensDePaiement(codAnu,e.getCod_ind(), m2.get("NUMOCCSQR"));
				if(mdps!=null && !mdps.isEmpty() && StringUtils.hasText(mdps.get(0))){
					q.setLic_mdp1_quittance2(mdps.get(0));
					if(mdps.size()>1 && StringUtils.hasText(mdps.get(1))){
						q.setLic_mdp2_quittance2(mdps.get(1));
					}
				}
			}
		}

		q.setPmt_3x(false);
		// récupérer info sur le paiement (3X, dates et montants)
		if(ssoApogeeService.isPaiement3X(codAnu,e.getCod_ind())){
			q.setPmt_3x(true);
			// récupérer info sur le paiement (3X, dates et montants)
			String d1 = ssoApogeeService.getDate1erPaiement(codAnu,e.getCod_ind());
			String d2 = ssoApogeeService.getDate2emPaiement(codAnu,e.getCod_ind());
			String d3 = ssoApogeeService.getDate3emPaiement(codAnu,e.getCod_ind());
			if(StringUtils.hasText(d1)){
				q.setDat_pmt1(d1);
				// récupérer info sur le montant
				q.setMnt_pmt1(ssoApogeeService.getMontant1erPaiement(codAnu,e.getCod_ind()));
			}
			if(StringUtils.hasText(d2)){
				q.setDat_pmt2(d2);
				// récupérer info sur le montant
				q.setMnt_pmt2(ssoApogeeService.getMontant2emPaiement(codAnu,e.getCod_ind()));
			}
			if(StringUtils.hasText(d3)){
				q.setDat_pmt3(d3);
				// récupérer info sur le montant
				q.setMnt_pmt3(ssoApogeeService.getMontant3emPaiement(codAnu,e.getCod_ind()));
			}

		}

		// récupérer montant total des droits payés
		q.setMnt_total(ssoApogeeService.getMontantTotalPaye(codAnu,e.getCod_ind()));
		// récupérer le détail des droits payés
		List<Map<String,String>> ldp = ssoApogeeService.getMontantsPayes(codAnu,e.getCod_ind());
		q.setList_droits_payes(convertResultToListDroitUniversitaire(ldp));

		e.setQuittance_sso(q);

		return true;
	}

	private List<DroitUniversitaire> convertResultToListDroitUniversitaire(List<Map<String, String>> ldp) {
		if(ldp!=null && !ldp.isEmpty()){
			List<DroitUniversitaire> l = new LinkedList<DroitUniversitaire>();
			for(Map<String, String> m : ldp){
				DroitUniversitaire du = new DroitUniversitaire();
				du.setLic_droit_paye(m.get("LIC_NRG"));
				du.setMnt_droit_paye(m.get("MONTANT"));
				l.add(du);
			}
			return l;
		}
		return null;
	}

	/**
	 * va chercher et renseigne les informations concernant l'affilication à la sécu
	 */
	public boolean recupererInfoAffiliationSso(String codAnu,Etudiant e){
		log.debug("-recupererInfoAffiliationSso pour codetu "+e.getCod_etu());
		List<Inscription> lins = e.getLinsciae();
		AffiliationSSO affiliation = new AffiliationSSO();
		//Récupérer les informations nécessaires dans apogée
		//boolean isAffilieSso = ssoApogeeService.isAffilieSso(codAnuIns, e.getCod_ind());
		Map<String,String> r = ssoApogeeService.getCentrePayeur(codAnu, e.getCod_ind(), e.isAffilieSso());
		affiliation.setCentre_payeur(r.get("LIC_CTP"));
		affiliation.setMutuelle(ssoApogeeService.getMutuelle(codAnu, e.getCod_ind()));

		for(Inscription ins : lins){
			// test si ins année en cours autorisées 
			if(ins.isEstEnRegle() && ins.isEstEnCours() && ins.getCod_anu().substring(0, 4).equals(codAnu)){
				//récupérer le libellé court de la composante
				String cmp = inscriptionService.getLicCmpFromCodIndIAE(codAnu, e.getCod_ind(), ins.getCod_etp(), ins.getCod_vrs_vet());
				// récupérer le libellé court de l'étape
				String etape = multipleApogeeService.getLibelleCourtEtape(ins.getCod_etp());
				if(affiliation.getList_lib_cmp_etape()==null){
					affiliation.setList_lib_cmp_etape(new LinkedList<LibCmpEtape>());
				}
				affiliation.getList_lib_cmp_etape().add(new LibCmpEtape(cmp,etape));
			}
		}

		// Récupérer la date de cotisation
		affiliation.setDat_cotisation(ssoApogeeService.getDateCotisation(codAnu, e.getCod_ind()));

		// date d'effet
		affiliation.setDat_effet(r.get("DAT_AFL_SSO"));

		e.setAffilition_sso(affiliation);

		if(affiliation.getDat_cotisation() == null) {
			return false;
		}

		return true;

	}


	public com.vaadin.server.Resource exportQuittancePdf(Etudiant e, Inscription inscription) {
		log.debug("-generation pdf Quittance pour "+e.getCod_etu());

		// verifie les autorisations
		if(!etudiantController.proposerQuittanceDroitsPayes(inscription, MainUI.getCurrent().getEtudiant())){
			return null;
		}

		String nomFichier = applicationContext.getMessage("pdf.quittance.title", null, Locale.getDefault())+"_" + inscription.getCod_anu().replace('/', '-') + "_" + MainUI.getCurrent().getEtudiant().getNom().replace('.', ' ').replace(' ', '_') + ".pdf";
		nomFichier = nomFichier.replaceAll(" ","_");

		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				try {
					ByteArrayOutputStream baosPDF = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
					PdfWriter docWriter = null;
					Document document = configureDocument();
					docWriter = PdfWriter.getInstance(document, baosPDF);
					// Test si on doit activer l'encryption
					byte[] ownerPwd = PdfUtils.generatePwd();
					if(PropertyUtils.isEnablePdfSecurity()){
						docWriter.setEncryption(null, ownerPwd, PdfWriter.AllowPrinting, PdfWriter.ENCRYPTION_AES_128);
					}
					docWriter.setStrictImageSequence(true);
					creerPdfQuittance(document,MainUI.getCurrent().getEtudiant(), inscription);
					docWriter.close();
					baosPDF.close();
					if(configController.isSignaturePdfQuittance()) {
						//Creation de l'export après ajout de signature
						return new ByteArrayInputStream(PdfUtils.signPdf(new PdfReader(baosPDF.toByteArray(), ownerPwd), configController.isSignatureAltPdfQuittance(), configController.getSignatureAltPositionQuittance()).toByteArray());
					} else {
						//Creation de l'export
						return new ByteArrayInputStream(baosPDF.toByteArray());
					}
				} catch (DocumentException e) {
					log.error("Erreur à la génération de l'attestation : DocumentException ",e);
					return null;
				} catch (IOException e) {
					log.error("Erreur à la génération de l'attestation : IOException ",e);
					return null;
				}

			}
		};

		// Création de la ressource 
		StreamResource resource = new StreamResource(source, nomFichier);
		resource.setMIMEType("application/force-download;charset=UTF-8");
		resource.setCacheTime(0);
		return resource;
	}

	public com.vaadin.server.Resource exportAffiliationSsoPdf(Etudiant e, Inscription inscription) {
		log.debug("-generation pdf AffiliationSso pour "+e.getCod_etu());


		// verifie les autorisations
		if(!etudiantController.proposerAttestationAffiliationSSO(inscription, MainUI.getCurrent().getEtudiant())){
			return null;
		}


		String nomFichier = applicationContext.getMessage("pdf.affiliationsso.title", null, Locale.getDefault())+"_" + inscription.getCod_anu().replace('/', '-') + "_" + MainUI.getCurrent().getEtudiant().getNom().replace('.', ' ').replace(' ', '_') + ".pdf";
		nomFichier = nomFichier.replaceAll(" ","_");

		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				try {
					ByteArrayOutputStream baosPDF = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
					PdfWriter docWriter = null;
					Document document = configureDocument();
					docWriter = PdfWriter.getInstance(document, baosPDF);
					// Test si on doit activer l'encryption
					if(PropertyUtils.isEnablePdfSecurity()){
						docWriter.setEncryption(null, null, PdfWriter.AllowPrinting, PdfWriter.ENCRYPTION_AES_128);
					}
					docWriter.setStrictImageSequence(true);
					creerPdfAffiliationSso(document,MainUI.getCurrent().getEtudiant(), inscription);
					docWriter.close();
					baosPDF.close();
					//Creation de l'export
					byte[] bytes = baosPDF.toByteArray();
					return new ByteArrayInputStream(bytes);
				} catch (DocumentException e) {
					log.error("Erreur à la génération de l'attestation : DocumentException ",e);
					return null;
				} catch (IOException e) {
					log.error("Erreur à la génération de l'attestation : IOException ",e);
					return null;
				}

			}
		};

		// Création de la ressource 
		StreamResource resource = new StreamResource(source, nomFichier);
		resource.setMIMEType("application/force-download;charset=UTF-8");
		resource.setCacheTime(0);
		return resource;
	}

	/**
	 * 
	 * @param document pdf
	 */
	public void creerPdfQuittance(final Document document, Etudiant etudiant, Inscription inscription) {
		//configuration des fonts
		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
		Font normalBig = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
		Font normalBigger = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
		Font header = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
		Font lightheader = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL);

		document.open();
		try {

			// Ajout Bordeaux1
			if (configController.isCertScolUtiliseLogo()){
				//ajout image test
				if (StringUtils.hasText(configController.getLogoUniversitePdf())){
					Image imageLogo = Image.getInstance(configController.getLogoUniversitePdf());

					int largeurLogo = configController.getLogoUniversitePdfDimension();
					float scaleRatio = largeurLogo / imageLogo.getWidth(); 
					float newHeight = scaleRatio * imageLogo.getHeight();
					imageLogo.scaleAbsolute(largeurLogo, newHeight);

					imageLogo.setAbsolutePosition(configController.getLogoUniversitePdfPortraitPositionX(), configController.getLogoUniversitePdfPortraitPositionY());
					document.add(imageLogo);
				}
				else if (StringUtils.hasText(configController.getHeaderPdf())) {
					Image imageHeader = Image.getInstance(configController.getHeaderPdf());

					int largeurHeader = configController.getDimensionPDFHeaderFooter();
					float scaleHeader = largeurHeader / imageHeader.getWidth();
					float newHeigthHeader = scaleHeader * imageHeader.getHeight();
					imageHeader.scaleAbsolute(largeurHeader, newHeigthHeader);

					document.add(imageHeader);
				}

				if (StringUtils.hasText(configController.getFooterPdf())) {
					Image imageFooter = Image.getInstance(configController.getFooterPdf());

					int largeurFooter = configController.getDimensionPDFHeaderFooter();
					float scaleFooter = largeurFooter / imageFooter.getWidth();
					float newHeigthHeader = scaleFooter * imageFooter.getHeight();
					imageFooter.scaleAbsolute(largeurFooter, newHeigthHeader);

					imageFooter.setAbsolutePosition(0, 0);
					document.add(imageFooter);
				}
			}

			//titre
			Paragraph pTitre = new Paragraph("\n\n"+applicationContext.getMessage("pdf.quittance.title.long", null, Locale.getDefault()).toUpperCase(), header);
			pTitre.setAlignment(Element.ALIGN_CENTER);
			document.add(pTitre);

			//Année universitaire
			Paragraph pAnnee = new Paragraph(inscription.getCod_anu(), lightheader);
			pAnnee.setAlignment(Element.ALIGN_CENTER);
			document.add(pAnnee);

			//Nom Prénom
			if (etudiant.getNom() != null) {
				Paragraph pNom = new Paragraph("\n\n"+etudiant.getNom(), normalBigger);
				// Si on doit utiliser les données d'état-civil
				if (etudiant.isTemPrUsage() && configController.isQuittanceDroitsPayesUsageEtatCivil()) {
					//On utilise les données d'état-civil
					pNom = new Paragraph("\n\n"+etudiant.getPrenomEtatCiv()+ " " + etudiant.getNomAffichage(), normalBigger);
				}
				pNom.setAlignment(Element.ALIGN_LEFT);
				document.add(pNom);
			}

			// Lieu et date de naissance
			if(configController.isQuittancePdfNaissance() && StringUtils.hasText(etudiant.getDatenaissance())) {
				Paragraph pNaissance = new Paragraph(applicationContext.getMessage("pdf.quittance.datenaissance", null, Locale.getDefault())+" ", normal);
				Chunk dateNaissance = new Chunk(etudiant.getDatenaissance(), normalBig);
				pNaissance.add(dateNaissance);
				if (StringUtils.hasText(etudiant.getLieunaissance()) && StringUtils.hasText(etudiant.getDepartementnaissance())) {
					Chunk lieuNaissance0 = new Chunk(" "+applicationContext.getMessage("pdf.quittance.lieunaissance", null, Locale.getDefault()) + " ", normal);
					pNaissance.add(lieuNaissance0);
					Chunk lieuNaissance1 = new Chunk(etudiant.getLieunaissance() + " (" + etudiant.getDepartementnaissance() + ")", normalBig);
					pNaissance.add(lieuNaissance1);
				}
				document.add(pNaissance);
			}

			//INE
			if (etudiant.getCod_nne() != null) {
				Paragraph pNNE = new Paragraph(applicationContext.getMessage("pdf.quittance.ine", null, Locale.getDefault())+" : ", normal);
				pNNE.setAlignment(Element.ALIGN_LEFT);
				Chunk nneText = new Chunk(etudiant.getCod_nne().toUpperCase(), normalBig);
				pNNE.add(nneText);
				Chunk codetuText = new Chunk("\t\t\t "+applicationContext.getMessage("pdf.quittance.codetu", null, Locale.getDefault())+" : ", normal);
				pNNE.add(codetuText);
				Chunk codetuVal = new Chunk(etudiant.getCod_etu(), normalBig);
				pNNE.add(codetuVal);
				document.add(pNNE);
			}

			Paragraph pBlank = new Paragraph(" ", normal);
			document.add(pBlank);


			//quittance
			if (etudiant.getQuittance_sso()!=null) {
				PdfPTable table = new PdfPTable(2);
				table.setWidthPercentage(98);

				//Num de quittance 1
				Paragraph pquit = new Paragraph(applicationContext.getMessage("pdf.quittance.num", null, Locale.getDefault())+" : ", normal);
				pquit.setAlignment(Element.ALIGN_LEFT);
				Chunk quitText = new Chunk(etudiant.getQuittance_sso().getNum_quittance1(), normalBig);
				pquit.add(quitText);
				PdfPCell cell = new PdfPCell(pquit);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);

				//Num de quittance 2
				PdfPCell cell2 = new PdfPCell();
				if(StringUtils.hasText(etudiant.getQuittance_sso().getNum_quittance2())) {
					Paragraph pquit2 = new Paragraph(applicationContext.getMessage("pdf.quittance.num", null, Locale.getDefault())+" : ", normal);
					Chunk quitText2 = new Chunk(etudiant.getQuittance_sso().getNum_quittance2(), normalBig);
					pquit2.add(quitText2);
					cell2 = new PdfPCell(pquit2);
				}
				cell2.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell2);

				//Date de quittance 1
				Paragraph pdatquit = new Paragraph(applicationContext.getMessage("pdf.quittance.date", null, Locale.getDefault())+" : ", normal);
				pdatquit.setAlignment(Element.ALIGN_LEFT);
				Chunk quitDat = new Chunk(etudiant.getQuittance_sso().getDat_quittance1(), normalBig);
				pdatquit.add(quitDat);
				PdfPCell cell3 = new PdfPCell(pdatquit);
				cell3.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell3);

				//Date de quittance 2
				PdfPCell cell4 = new PdfPCell();
				if(StringUtils.hasText(etudiant.getQuittance_sso().getDat_quittance2())) {
					Paragraph pdatquit2 = new Paragraph(applicationContext.getMessage("pdf.quittance.date", null, Locale.getDefault())+" : ", normal);
					Chunk quitDat2 = new Chunk(etudiant.getQuittance_sso().getDat_quittance2(), normalBig);
					pdatquit2.add(quitDat2);
					cell4 = new PdfPCell(pdatquit2);
				}
				cell4.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell4);

				//cge1
				//if(etudiant.getQuittance_sso().getLic_cge_quittance1()!=null){
				Paragraph pcge = new Paragraph(applicationContext.getMessage("pdf.quittance.cge", null, Locale.getDefault()), normal);
				Chunk cgeValue = new Chunk(etudiant.getQuittance_sso().getLic_cge_quittance1()!=null?etudiant.getQuittance_sso().getLic_cge_quittance1():"", normalBig);
				pcge.add(cgeValue);
				pcge.setAlignment(Element.ALIGN_LEFT);
				PdfPCell cell5 = new PdfPCell(pcge);
				cell5.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell5);
				//}

				//cge2
				//if(etudiant.getQuittance_sso().getLic_cge_quittance2()!=null){
				Paragraph pcge2 = new Paragraph(applicationContext.getMessage("pdf.quittance.cge", null, Locale.getDefault()), normal);
				Chunk cge2Value = new Chunk(etudiant.getQuittance_sso().getLic_cge_quittance2()!=null?etudiant.getQuittance_sso().getLic_cge_quittance2():"", normalBig);
				pcge2.add(cge2Value);
				pcge2.setAlignment(Element.ALIGN_LEFT);
				PdfPCell cell6 = new PdfPCell(pcge2);
				cell6.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell6);
				//}

				// mdp1 q1
				//if(etudiant.getQuittance_sso().getLic_mdp1_quittance1()!=null){
				Paragraph pmdp1q1 = new Paragraph(applicationContext.getMessage("pdf.quittance.mdp", null, Locale.getDefault()), normal);
				Chunk mdp1q1Value = new Chunk(etudiant.getQuittance_sso().getLic_mdp1_quittance1()!=null?etudiant.getQuittance_sso().getLic_mdp1_quittance1():"", normalBig);
				pmdp1q1.add(mdp1q1Value);
				pmdp1q1.setAlignment(Element.ALIGN_LEFT);
				PdfPCell cell7 = new PdfPCell(pmdp1q1);
				cell7.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell7);
				//}


				// mdp1 q2
				//if(etudiant.getQuittance_sso().getLic_mdp1_quittance2()!=null){
				Paragraph pmdp1q2 = new Paragraph(applicationContext.getMessage("pdf.quittance.mdp", null, Locale.getDefault()), normal);
				Chunk mdp1q2Value = new Chunk(etudiant.getQuittance_sso().getLic_mdp1_quittance2()!=null?etudiant.getQuittance_sso().getLic_mdp1_quittance2():"", normalBig);
				pmdp1q2.add(mdp1q2Value);
				pmdp1q2.setAlignment(Element.ALIGN_LEFT);
				PdfPCell cell8 = new PdfPCell(pmdp1q2);
				cell8.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell8);
				//}

				// mdp2 q1
				//if(etudiant.getQuittance_sso().getLic_mdp2_quittance1()!=null){
				Paragraph pmdp2q1 = new Paragraph(applicationContext.getMessage("pdf.quittance.mdp", null, Locale.getDefault()), normal);
				Chunk mdp2q1Value = new Chunk(etudiant.getQuittance_sso().getLic_mdp2_quittance1()!=null?etudiant.getQuittance_sso().getLic_mdp2_quittance1():"", normalBig);
				pmdp2q1.add(mdp2q1Value);
				pmdp2q1.setAlignment(Element.ALIGN_LEFT);
				PdfPCell cell9 = new PdfPCell(pmdp2q1);
				cell9.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell9);
				//}

				// mdp2 q2
				//if(etudiant.getQuittance_sso().getLic_mdp2_quittance2()!=null){
				Paragraph pmdp2q2 = new Paragraph(applicationContext.getMessage("pdf.quittance.mdp", null, Locale.getDefault()), normal);
				Chunk mdp2q2Value = new Chunk(etudiant.getQuittance_sso().getLic_mdp2_quittance2()!=null?etudiant.getQuittance_sso().getLic_mdp2_quittance2():"", normalBig);
				pmdp2q2.add(mdp2q2Value);
				pmdp2q2.setAlignment(Element.ALIGN_LEFT);
				PdfPCell cell10 = new PdfPCell(pmdp2q2);
				cell10.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell10);
				//}

				document.add(table);
				//Si paiement en 3X
				if(etudiant.getQuittance_sso().isPmt_3x()){
					//Ajout bloc sur le paiement en 3X
					String p1 =etudiant.getQuittance_sso().getMnt_pmt1() + applicationContext.getMessage("pdf.quittance.p3x.euro", null, Locale.getDefault());
					String p2 = etudiant.getQuittance_sso().getMnt_pmt2() + applicationContext.getMessage("pdf.quittance.p3x.euro", null, Locale.getDefault());
					String p3 = etudiant.getQuittance_sso().getMnt_pmt3() + applicationContext.getMessage("pdf.quittance.p3x.euro", null, Locale.getDefault());
					String d1 =etudiant.getQuittance_sso().getDat_pmt1();
					String d2 = etudiant.getQuittance_sso().getDat_pmt2();
					String d3 = etudiant.getQuittance_sso().getDat_pmt3();
					//Si 3x le même montant
					if(p1.equals(p2) && p2.equals(p3)){
						Paragraph p3xpmt = new Paragraph("\n"+applicationContext.getMessage("pdf.quittance.pmt3x.simple", new Object[] {p1,d1,d2,d3}, Locale.getDefault()), normal);
						p3xpmt.setAlignment(Element.ALIGN_LEFT);
						document.add(p3xpmt);
					}else{
						//Si 1er pmt différent des 2 autres
						if(!p1.equals(p2) && p2.equals(p3)){
							Paragraph p3xpmt = new Paragraph("\n"+applicationContext.getMessage("pdf.quittance.pmt3x.modere", new Object[] {p1,d1,p2,d2,d3}, Locale.getDefault()), normal);
							p3xpmt.setAlignment(Element.ALIGN_LEFT);
							document.add(p3xpmt);
						}else{
							//3 paiements différents
							Paragraph p3xpmt = new Paragraph("\n"+applicationContext.getMessage("pdf.quittance.pmt3x.complexe", new Object[] {p1,d1,p2,d2,p3,d3}, Locale.getDefault()), normal);
							p3xpmt.setAlignment(Element.ALIGN_LEFT);
							document.add(p3xpmt);
						}
					}
				}


				PdfPTable table2 = new PdfPTable(2);
				table2.setWidthPercentage(98);
				// Détail des droits payés
				if(etudiant.getQuittance_sso().getList_droits_payes() != null && !etudiant.getQuittance_sso().getList_droits_payes().isEmpty()){
					Paragraph ptextDetail = new Paragraph("\n"+applicationContext.getMessage("pdf.quittance.txtdetail", null, Locale.getDefault())+" :", normal);
					ptextDetail.setAlignment(Element.ALIGN_LEFT);
					document.add(ptextDetail);


					int i=1;
					for(DroitUniversitaire d : etudiant.getQuittance_sso().getList_droits_payes()){
						Paragraph pmnt = new Paragraph(d.getLic_droit_paye(), normal);
						pmnt.setAlignment(Element.ALIGN_LEFT);
						PdfPCell celld1 = new PdfPCell(pmnt);
						celld1.setBorder(Rectangle.NO_BORDER);
						table2.addCell(celld1);

						Paragraph pmnt2 = new Paragraph(d.getMnt_droit_paye() + applicationContext.getMessage("pdf.quittance.detail.euro", null, Locale.getDefault()), normal);
						pmnt2.setAlignment(Element.ALIGN_RIGHT);
						PdfPCell celld2 = new PdfPCell(pmnt2);
						celld2.setHorizontalAlignment(Element.ALIGN_RIGHT);
						celld2.setBorder(Rectangle.NO_BORDER);

						//Si dernier élément de la table
						if(i==etudiant.getQuittance_sso().getList_droits_payes().size()){
							// ajout border bottom
							celld2.setBorder(Rectangle.BOTTOM);
						}

						table2.addCell(celld2);
						i++;

					}
				}

				//Total des droits payés
				if(etudiant.getQuittance_sso().getMnt_total()!=null){
					Paragraph pmnttotal = new Paragraph(applicationContext.getMessage("pdf.quittance.mnttotal", null, Locale.getDefault()), normalBig);
					pmnttotal.setAlignment(Element.ALIGN_LEFT);
					PdfPCell celltt = new PdfPCell(pmnttotal);
					celltt.setBorder(Rectangle.NO_BORDER);
					table2.addCell(celltt);

					Paragraph pmnttotal2 = new Paragraph(etudiant.getQuittance_sso().getMnt_total() + applicationContext.getMessage("pdf.quittance.total.euro", null, Locale.getDefault()), normalBig);
					pmnttotal2.setAlignment(Element.ALIGN_RIGHT);
					PdfPCell celltt2 = new PdfPCell(pmnttotal2);
					celltt2.setBorder(Rectangle.NO_BORDER);
					celltt2.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table2.addCell(celltt2);

				}
				document.add(table2);


				// Si on doit ajouter la signature sur la quittance
				if(configController.isQuittancePdfSignature()) {
					document.add(new Paragraph(" "));
					Signataire signataire = multipleApogeeService.getSignataireQuittance(configController.getQuittanceCodeSignataire(), PropertyUtils.getClefApogeeDecryptBlob(), configController.isQuittanceSignatureTampon());

					String nomSignataire = StringUtils.hasText(configController.getQuittanceDescSignataire())? configController.getQuittanceDescSignataire() : signataire.getQua_sig() + " " + signataire.getNom_sig();

					//date
					Date d = new Date();
					DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					String date = dateFormat.format(d);

					float[] widthsSignataire = {2f, 1.3f};
					PdfPTable tableSignataire = new PdfPTable(widthsSignataire);
					tableSignataire.setWidthPercentage(100f);
					tableSignataire.addCell(makeCellSignataire("", normal));
					tableSignataire.addCell(makeCellSignataire(applicationContext.getMessage("pdf.quittance.fait1", null, Locale.getDefault())+" " + configController.getCertScolLieuEdition() + applicationContext.getMessage("pdf.quittance.fait2", null, Locale.getDefault())+" " + date, normal));
					tableSignataire.addCell(makeCellSignataire("", normal));
					tableSignataire.addCell(makeCellSignataire(nomSignataire, normal));

					document.add(tableSignataire);

					//ajout signature
					if (signataire.getImg_sig_std() != null && signataire.getImg_sig_std().length > 0){ 
						log.debug(signataire.getImg_sig_std().toString());
						Image imageSignature = Image.getInstance(signataire.getImg_sig_std());

						int largeurSignature = configController.getDimensionPDFSignature();
						float scaleRatio = largeurSignature / imageSignature.getWidth(); 
						float newHeight = scaleRatio * imageSignature.getHeight();
						imageSignature.scaleAbsolute(largeurSignature, newHeight);

						imageSignature.setAlignment(Element.ALIGN_RIGHT);
						imageSignature.setIndentationRight(30);
						document.add(imageSignature);

					} else {
						log.warn("Signature de "+configController.getQuittanceCodeSignataire()+" vide ou non récupérée pour la génération de la quittance");
					}
				}


			}


		} catch (BadElementException e) {
			log.error("Erreur à la génération de la quittance : BadElementException ",e);
		}  catch (DocumentException e) {
			log.error("Erreur à la génération de la quittance : DocumentException ",e);
		} catch (MalformedURLException e) {
			log.error("Erreur à la génération de la quittance : MalformedURLException ",e);
		} catch (IOException e) {
			log.error("Erreur à la génération de la quittance : IOException ",e);
		}
		// step 6: fermeture du document.
		document.close();
	}
	/**
	 * 
	 * @param document pdf
	 */
	public void creerPdfAffiliationSso(final Document document, Etudiant etudiant, Inscription inscription) {
		//configuration des fonts
		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
		Font normalBig = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
		Font normalBigger = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
		Font header = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
		Font lightheader = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL);

		document.open();
		try {

			// Ajout Bordeaux1
			if (configController.isCertScolUtiliseLogo()){
				//ajout image test
				if (StringUtils.hasText(configController.getLogoUniversitePdf())){
					Image imageLogo = Image.getInstance(configController.getLogoUniversitePdf());

					int largeurLogo = configController.getLogoUniversitePdfDimension();
					float scaleRatio = largeurLogo / imageLogo.getWidth(); 
					float newHeight = scaleRatio * imageLogo.getHeight();
					imageLogo.scaleAbsolute(largeurLogo, newHeight);

					imageLogo.setAbsolutePosition(configController.getLogoUniversitePdfPortraitPositionX(), configController.getLogoUniversitePdfPortraitPositionY());
					document.add(imageLogo);
				}
				else if (StringUtils.hasText(configController.getHeaderPdf())) {
					Image imageHeader = Image.getInstance(configController.getHeaderPdf());

					int largeurHeader = configController.getDimensionPDFHeaderFooter();
					float scaleHeader = largeurHeader / imageHeader.getWidth();
					float newHeigthHeader = scaleHeader * imageHeader.getHeight();
					imageHeader.scaleAbsolute(largeurHeader, newHeigthHeader);

					imageHeader.setAbsolutePosition(0, 765);
					document.add(imageHeader);
				}

				if (StringUtils.hasText(configController.getFooterPdf())) {
					Image imageFooter = Image.getInstance(configController.getFooterPdf());

					int largeurFooter = configController.getDimensionPDFHeaderFooter();
					float scaleFooter = largeurFooter / imageFooter.getWidth();
					float newHeigthHeader = scaleFooter * imageFooter.getHeight();
					imageFooter.scaleAbsolute(largeurFooter, newHeigthHeader);

					imageFooter.setAbsolutePosition(0, 0);
					document.add(imageFooter);
				}
			}

			//titre
			Paragraph pTitre = new Paragraph("\n\n"+applicationContext.getMessage("pdf.affiliationsso.title.long", null, Locale.getDefault()).toUpperCase(), header);
			pTitre.setAlignment(Element.ALIGN_CENTER);
			document.add(pTitre);

			//Année universitaire
			Paragraph pAnnee = new Paragraph(inscription.getCod_anu(), lightheader);
			pAnnee.setAlignment(Element.ALIGN_CENTER);
			document.add(pAnnee);

			//Nom Prénom
			if (etudiant.getNom() != null) {
				Paragraph pNom = new Paragraph("\n\n"+etudiant.getNom(), normalBigger);

				// Si on doit utiliser les données d'état-civil
				if (etudiant.isTemPrUsage() && configController.isAffiliationSsoUsageEtatCivil()) {
					//On utilise les données d'état-civil
					pNom = new Paragraph("\n\n"+etudiant.getPrenomEtatCiv()+ " " + etudiant.getNomAffichage(), normalBigger);
				}

				pNom.setAlignment(Element.ALIGN_LEFT);
				document.add(pNom);
			}

			//Date de naissance
			if (etudiant.getDatenaissance() != null) {
				Paragraph pDateNaissance = new Paragraph(applicationContext.getMessage("pdf.affiliationsso.naissance1", null, Locale.getDefault())+" " , normal);
				pDateNaissance.setAlignment(Element.ALIGN_LEFT);
				Chunk datenaissanceText = new Chunk(etudiant.getDatenaissance(),normalBig);
				pDateNaissance.add(datenaissanceText);
				document.add(pDateNaissance);
			}

			//INE
			if (etudiant.getCod_nne() != null) {
				Paragraph pNNE = new Paragraph(applicationContext.getMessage("pdf.affiliationsso.ine", null, Locale.getDefault())+" : ", normal);
				pNNE.setAlignment(Element.ALIGN_LEFT);
				Chunk nneText = new Chunk(etudiant.getCod_nne().toUpperCase(), normalBig);
				pNNE.add(nneText);
				document.add(pNNE);
			}

			//Date de cotisation et date d'effet
			if (etudiant.getAffilition_sso()!=null  && etudiant.getAffilition_sso().getDat_cotisation() != null  && etudiant.getAffilition_sso().getDat_effet()!= null) {
				Chunk cotisation = new Chunk("\n"+applicationContext.getMessage("pdf.affiliationsso.cotisation", null, Locale.getDefault())+" : ",normal);
				Chunk cotisationText = new Chunk(etudiant.getAffilition_sso().getDat_cotisation(),normalBig);
				Chunk effet=new Chunk("\t\t"+applicationContext.getMessage("pdf.affiliationsso.effet", null, Locale.getDefault())+" : ",normal);
				Chunk effetText = new Chunk(etudiant.getAffilition_sso().getDat_effet(),normalBig);
				Paragraph pCotisation = new Paragraph();
				pCotisation.add(cotisation);
				pCotisation.add(cotisationText);
				pCotisation.add(effet);
				pCotisation.add(effetText);
				pCotisation.setAlignment(Element.ALIGN_LEFT);
				document.add(pCotisation);
			}

			if(etudiant.getAffilition_sso()!=null && !etudiant.getAffilition_sso().getList_lib_cmp_etape().isEmpty()){
				for(LibCmpEtape lce : etudiant.getAffilition_sso().getList_lib_cmp_etape()){
					// CMP + lib etape
					Paragraph pcge = new Paragraph(lce.getLib_cmp()+ "\t\t"+lce.getLib_etape(), normalBig);
					pcge.setAlignment(Element.ALIGN_LEFT);
					document.add(pcge);
				}
			}

			// Centre payeur
			if (etudiant.getAffilition_sso()!=null  && etudiant.getAffilition_sso().getCentre_payeur() != null) {
				Paragraph pcentre = new Paragraph("\n"+applicationContext.getMessage("pdf.affiliationsso.centrepayeur", null, Locale.getDefault())+" : ", normal);
				Chunk centreText = new Chunk(etudiant.getAffilition_sso().getCentre_payeur(),normalBig);
				pcentre.add(centreText);
				pcentre.setAlignment(Element.ALIGN_LEFT);
				document.add(pcentre);
			}

			//Mutuelle
			if (etudiant.getAffilition_sso()!=null  && etudiant.getAffilition_sso().getMutuelle() != null) {
				Paragraph pmutuelle = new Paragraph(applicationContext.getMessage("pdf.affiliationsso.mutuelle", null, Locale.getDefault())+" : ", normal);
				Chunk mutText = new Chunk(etudiant.getAffilition_sso().getMutuelle(),normalBig);
				pmutuelle.add(mutText);
				pmutuelle.setAlignment(Element.ALIGN_LEFT);
				document.add(pmutuelle);
			}

			//encart si affilié
			if(etudiant.getAffilition_sso()!=null && etudiant.isAffilieSso()){
				Paragraph pmutuelle = new Paragraph("\n\n"+applicationContext.getMessage("pdf.affiliationsso.message", null, Locale.getDefault()), normal);
				pmutuelle.setAlignment(Element.ALIGN_LEFT);
				document.add(pmutuelle);
			}



		} catch (BadElementException e) {
			log.error("Erreur à la génération de l'attestation SSO : BadElementException ",e);
		}  catch (DocumentException e) {
			log.error("Erreur à la génération de l'attestation SSO : DocumentException ",e);
		} catch (MalformedURLException e) {
			log.error("Erreur à la génération de l'attestation SSO : MalformedURLException ",e);
		} catch (IOException e) {
			log.error("Erreur à la génération de l'attestation SSO : IOException ",e);
		}
		// step 6: fermeture du document.
		document.close();
	}

	private PdfPCell makeCell(String str, Font font) {
		PdfPCell cell = new PdfPCell();
		cell.setBorder(0);
		cell.setPhrase(new Phrase(str, font));
		return cell;
	}
	private PdfPCell makeCellSignataire(String str, Font font) {
		PdfPCell cell = makeCell(str, font);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		return cell;
	}

	/**
	 * configure le document pdf.
	 * @param width
	 * @param height
	 * @param margin
	 * @return doc
	 */
	private Document configureDocument() {

		Document document = new Document();

		document.setPageSize(PageSize.A4); // A6
		float marginTop = (MARGIN_TOP / 2.54f) * 72f;
		float marginRight = (MARGIN_RIGHT / 2.54f) * 72f;
		float marginBottom = (MARGIN_BOTTOM / 2.54f) * 72f;
		float marginLeft = (MARGIN_LEFT / 2.54f) * 72f;
		document.setMargins(marginLeft, marginRight, marginTop, marginBottom);

		return document;
	}
}
