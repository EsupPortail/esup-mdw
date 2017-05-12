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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.AffiliationSSO;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.services.apogee.InscriptionService;
import fr.univlorraine.mondossierweb.services.apogee.InscriptionServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.services.apogee.SsoApogeeService;

/**
 * Gestion des infos de sécu (affiliation et quittance des droits)
 */
@Component
public class SsoController {

	private Logger LOG = LoggerFactory.getLogger(SsoController.class);

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
	public void recupererInfoAffiliationSso(Etudiant e,Inscription ins){
		LOG.debug("-recupererInfoAffiliationSso pour codetu "+e.getCod_etu());
		String codAnuIns=ins.getCod_anu().substring(0, 4);
		AffiliationSSO affiliation = new AffiliationSSO();
		//Récupérer les informations nécessaires dans apogée
		boolean isAffilieSso = ssoApogeeService.isAffilieSso(codAnuIns, e.getCod_ind());
		Map<String,String> r = ssoApogeeService.getCentrePayeur(codAnuIns, e.getCod_ind(), isAffilieSso);
		affiliation.setCentre_payeur(r.get("LIC_CTP"));
		affiliation.setMutuelle(ssoApogeeService.getMutuelle(codAnuIns, e.getCod_ind()));
		//récupérer le libellé court de la composante
		affiliation.setCmp(inscriptionService.getLicCmpFromCodIndIAE(codAnuIns, e.getCod_ind(), ins.getCod_etp(), ins.getCod_vrs_vet()));
		// récupérer le libellé court de l'étape
		affiliation.setEtape(multipleApogeeService.getLibelleCourtEtape(ins.getCod_etp()));
		// Récupérer la date de cotisation
		affiliation.setDat_cotisation(ssoApogeeService.getDateCotisation(codAnuIns, e.getCod_ind()));
		// date d'effet
		affiliation.setDat_effet(r.get("DAT_AFL_SSO"));
		
		e.setAffilition_sso(affiliation);

	}

	/**
	 * va chercher et renseigne les informations concernant les droits payés par l'étudiant
	 */
	public void recupererInfoQuittanceDroitsPayes(Etudiant e){
		LOG.debug("-recupererInfoQuittanceDroitsPayes pour codetu "+e.getCod_etu());

	}

	public com.vaadin.server.Resource exportAffiliationSsoPdf(Etudiant e, Inscription inscription) {
		LOG.debug("-generation pdf AffiliationSso pour "+e.getCod_etu());

		recupererInfoAffiliationSso(e, inscription);

		// verifie les autorisations
		if(!etudiantController.proposerAttestationAffiliationSSO(inscription, MainUI.getCurrent().getEtudiant())){
			return null;
		}


		String nomFichier = applicationContext.getMessage("pdf.affiliationsso.title", null, Locale.getDefault())+"_" + inscription.getCod_anu().replace('/', '-') + "_" + MainUI.getCurrent().getEtudiant().getNom().replace('.', ' ').replace(' ', '_') + ".pdf";
		nomFichier = nomFichier.replaceAll(" ","_");

		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getStream() {
				try {
					ByteArrayOutputStream baosPDF = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
					PdfWriter docWriter = null;
					Document document = configureDocument();
					docWriter = PdfWriter.getInstance(document, baosPDF);
					docWriter.setEncryption(null, null, PdfWriter.AllowPrinting, PdfWriter.ENCRYPTION_AES_128);
					docWriter.setStrictImageSequence(true);
					creerPdfAffiliationSso(document,MainUI.getCurrent().getEtudiant(), inscription);
					docWriter.close();
					baosPDF.close();
					//Creation de l'export
					byte[] bytes = baosPDF.toByteArray();
					return new ByteArrayInputStream(bytes);
				} catch (DocumentException e) {
					LOG.error("Erreur à la génération de l'attestation : DocumentException ",e);
					return null;
				} catch (IOException e) {
					LOG.error("Erreur à la génération de l'attestation : IOException ",e);
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
	public void creerPdfAffiliationSso(final Document document, Etudiant etudiant, Inscription inscription) {
		//configuration des fonts
		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
		Font normalBig = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
		Font normalBigger = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
		Font header = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);
		Font lightheader = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL);

		//DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		document.open();
		try {

			// Ajout Bordeaux1
			if (configController.isCertScolUtiliseLogo()){
				//ajout image test
				if (configController.getLogoUniversitePdf() != null && !configController.getLogoUniversitePdf().equals("")){
					Image imageLogo = Image.getInstance(configController.getLogoUniversitePdf());
					float scaleRatio = 40 / imageLogo.getHeight(); 
					float newWidth=scaleRatio * imageLogo.getWidth();
					imageLogo.scaleAbsolute(newWidth, 40);
					imageLogo.setAbsolutePosition(100, 750);
					document.add(imageLogo);
				}
				else if (configController.getCertScolHeaderUniv() != null && !configController.getCertScolHeaderUniv().equals("")) {
					Image imageHeader = Image.getInstance(configController.getCertScolHeaderUniv());
					float scaleHeader = 600 / imageHeader.getWidth();
					float newHeigthHeader = scaleHeader * imageHeader.getHeight();
					imageHeader.scaleAbsolute(600, newHeigthHeader);
					imageHeader.setAbsolutePosition(0, 765);
					document.add(imageHeader);
				}

				if (configController.getCertScolFooter() != null && !configController.getCertScolFooter().equals("")) {
					Image imageFooter = Image.getInstance(configController.getCertScolFooter());
					float scaleFooter = 600 / imageFooter.getWidth();
					float newHeigthFooter = scaleFooter * imageFooter.getHeight();
					imageFooter.scaleAbsolute(600, newHeigthFooter);
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
				Paragraph pNom = new Paragraph(etudiant.getNom(), normalBigger);
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
				Chunk nneText = new Chunk(etudiant.getCod_nne().toLowerCase(), normalBig);
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
			
			// CMP + lib etape
			if (etudiant.getAffilition_sso()!=null  && etudiant.getAffilition_sso().getCmp() != null) {
				Paragraph pcge = new Paragraph(etudiant.getAffilition_sso().getCmp()+ "\t\t"+etudiant.getAffilition_sso().getEtape(), normalBig);
				pcge.setAlignment(Element.ALIGN_LEFT);
			    document.add(pcge);
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

			//encart
			Paragraph pmutuelle = new Paragraph("\n\n"+applicationContext.getMessage("pdf.affiliationsso.message", null, Locale.getDefault()), normal);
			pmutuelle.setAlignment(Element.ALIGN_LEFT);
			document.add(pmutuelle);



		} catch (BadElementException e) {
			LOG.error("Erreur à la génération du certificat : BadElementException ",e);
		}  catch (DocumentException e) {
			LOG.error("Erreur à la génération du certificat : DocumentException ",e);
		} catch (MalformedURLException e) {
			LOG.error("Erreur à la génération du certificat : MalformedURLException ",e);
		} catch (IOException e) {
			LOG.error("Erreur à la génération du certificat : IOException ",e);
		}
		// step 6: fermeture du document.
		document.close();
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
