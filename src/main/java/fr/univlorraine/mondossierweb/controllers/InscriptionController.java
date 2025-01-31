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

import jakarta.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;

import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.PdfUtils;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;

/**
 * Gestion des inscriptions et du certificat de scolarité
 */
@Component
public class InscriptionController {

	private Logger LOG = LoggerFactory.getLogger(InscriptionController.class);

	/**
	 * marges.
	 */
	private static final float MARGIN_TOP = 5.0f;
	private static final float MARGIN_RIGHT = 2.0f;
	private static final float MARGIN_BOTTOM = 4.0f;
	private static final float MARGIN_LEFT = 3.0f;
	/**
	 * outputstream size.
	 */
	private static final int OUTPUTSTREAM_SIZE = 1024;

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private MultipleApogeeService multipleApogeeService;
	@Resource
	private transient ConfigController configController;



	/**
	 * 
	 * @return le fichier pdf.
	 */
	public com.vaadin.server.Resource exportPdf(Inscription inscription) {

		// verifie les autorisations
		if(GenericUI.getCurrent() instanceof MainUI){
			if(!etudiantController.proposerCertificat(inscription, MainUI.getCurrent().getEtudiant(),false)){
				return null;
			}
		}
		if(GenericUI.getCurrent() instanceof MdwTouchkitUI){
			if(!etudiantController.proposerCertificat(inscription, MdwTouchkitUI.getCurrent().getEtudiant(),true)){
				return null;
			}
		}


		String nomFichier = applicationContext.getMessage("pdf.certificat.title", null, Locale.getDefault())+"_" + inscription.getCod_etp() + "_" + inscription.getCod_anu().replace('/', '-') + "_" + GenericUI.getCurrent().getEtudiant().getNom().replace('.', ' ').replace(' ', '_') + ".pdf";
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
					// Test si on doit activer l'encryption
					byte[] ownerPwd = PdfUtils.generatePwd();
					if(PropertyUtils.isEnablePdfSecurity()){
						docWriter.setEncryption(null, ownerPwd, PdfWriter.AllowPrinting, PdfWriter.ENCRYPTION_AES_128);
					}
					docWriter.setStrictImageSequence(true);
					creerPdfCertificatScolarite(document,GenericUI.getCurrent().getEtudiant(), inscription);
					docWriter.close();
					baosPDF.close();
					if(configController.isSignaturePdfCertificat()) {
						//Creation de l'export après ajout de signature
						return new ByteArrayInputStream(PdfUtils.signPdf(new PdfReader(baosPDF.toByteArray(), ownerPwd), configController.isSignatureAltPdfCertificat(), configController.getSignatureAltPositionCertificat()).toByteArray());
					} else {
						//Creation de l'export
						return new ByteArrayInputStream(baosPDF.toByteArray());
					}
				} catch (DocumentException e) {
					LOG.error("Erreur à la génération du certificat : DocumentException ",e);
					return null;
				} catch (IOException e) {
					LOG.error("Erreur à la génération du certificat : IOException ",e);
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
	 * configure le document pdf.
	 * @param width
	 * @param height
	 * @param margin
	 * @return doc
	 */
	private Document configureDocument() {

		Document document = new Document();

		document.setPageSize(PageSize.A4);
		float marginTop = (MARGIN_TOP / 2.54f) * 72f;
		float marginRight = (MARGIN_RIGHT / 2.54f) * 72f;
		float marginBottom = (MARGIN_BOTTOM / 2.54f) * 72f;
		float marginLeft = (MARGIN_LEFT / 2.54f) * 72f;
		document.setMargins(marginLeft, marginRight, marginTop, marginBottom);

		return document;
	}


	/**
	 * 
	 * @param document pdf
	 */
	public void creerPdfCertificatScolarite(final Document document, Etudiant etudiant, Inscription inscription) {

		//configuration des fonts
		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
		Font normalBig = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
		Font header = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD);

		//date
		Date d = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String date = dateFormat.format(d);

		document.open();
		try {


			Signataire signataire = multipleApogeeService.getSignataireCes(configController.getCertScolCodeSignataire(), PropertyUtils.getClefApogeeDecryptBlob());

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

			Paragraph pTitre = new Paragraph("\n\n"+applicationContext.getMessage("pdf.certificat.title", null, Locale.getDefault()).toUpperCase()+"\n"+inscription.getCod_anu(), header);
			pTitre.setAlignment(Element.ALIGN_CENTER);
			document.add(pTitre);

			String quaSignataire = StringUtils.hasText(configController.getCertScolDescSignataire())? configController.getCertScolDescSignataire() : signataire.getQua_sig();
			Paragraph pCertifie = new Paragraph("\n\n\n\n" + quaSignataire + " "+applicationContext.getMessage("pdf.certificat.certifie", null, Locale.getDefault())+"\n\n", normal);
			pCertifie.setAlignment(Element.ALIGN_LEFT);
			document.add(pCertifie);


			// Si on doit utiliser les données d'état-civil
			if (etudiant.isTemPrUsage() && configController.isCertScolUsageEtatCivil()) {
				//On utilise les données d'état-civil
				String civCertif = "";
				if (etudiant.getSexEtatCiv() != null) {
					if (etudiant.getSexEtatCiv().equals("M")) {
						civCertif = applicationContext.getMessage("pdf.certificat.civ1", null, Locale.getDefault());
					} else if (etudiant.getSexEtatCiv().equals("F")) {
						civCertif = applicationContext.getMessage("pdf.certificat.civ2", null, Locale.getDefault());
					}
				}
				Paragraph pNom = new Paragraph(civCertif + " " + etudiant.getPrenomEtatCiv() + " " + etudiant.getNomAffichage(), normalBig);
				pNom.setIndentationLeft(15);
				document.add(pNom);

			} else {
				// Si on ne doit pas utiliser les données d'état-civil
				String civCertif = "";
				if (etudiant.getCodCiv() != null) {
					if (etudiant.getCodCiv().equals("1")) {
						civCertif = applicationContext.getMessage("pdf.certificat.civ1", null, Locale.getDefault());
					} else if (etudiant.getCodCiv().equals("2")) {
						civCertif = applicationContext.getMessage("pdf.certificat.civ2", null, Locale.getDefault());
					}
				}
				Paragraph pNom = new Paragraph(civCertif + " " + etudiant.getNom(), normalBig);
				pNom.setIndentationLeft(15);
				document.add(pNom);
			} 

			if (etudiant.getCod_nne() != null) {
				Paragraph pNNE = new Paragraph("\n"+applicationContext.getMessage("pdf.certificat.id", null, Locale.getDefault())+" : " + etudiant.getCod_nne().toLowerCase(), normal);
				pNNE.setAlignment(Element.ALIGN_LEFT);
				document.add(pNNE);
			}
			if (etudiant.getCod_etu() != null) {
				Paragraph p01 = new Paragraph(applicationContext.getMessage("pdf.certificat.numetudiant", null, Locale.getDefault())+" : " + etudiant.getCod_etu(), normal);
				p01.setAlignment(Element.ALIGN_LEFT);
				document.add(p01);
			}
			if (etudiant.getDatenaissance() != null) {
				Paragraph pDateNaissance = new Paragraph(applicationContext.getMessage("pdf.certificat.naissance1", null, Locale.getDefault())+" " + etudiant.getDatenaissance(), normal);
				pDateNaissance.setAlignment(Element.ALIGN_LEFT);
				document.add(pDateNaissance);
			}
			if ((etudiant.getLieunaissance() != null) && (etudiant.getDepartementnaissance() != null)) {
				Paragraph pLieuNaissance = new Paragraph(applicationContext.getMessage("pdf.certificat.naissance2", null, Locale.getDefault())+" " + etudiant.getLieunaissance() + " (" + etudiant.getDepartementnaissance() + ")", normal);
				pLieuNaissance.setAlignment(Element.ALIGN_LEFT);
				document.add(pLieuNaissance);
			}

			String anneeEnCours = etudiantController.getAnneeUnivEnCoursToDisplay(GenericUI.getCurrent());
			String inscritCertif = "";
			if (inscription.getCod_anu().equals(anneeEnCours)) {
				inscritCertif = applicationContext.getMessage("pdf.certificat.inscrit", null, Locale.getDefault());
			} else {
				inscritCertif = applicationContext.getMessage("pdf.certificat.ete.inscrit", null, Locale.getDefault());
			}
			Paragraph pEstInscrit = new Paragraph("\n"+inscritCertif+" " + inscription.getCod_anu() + "\n ", normal);
			pEstInscrit.setAlignment(Element.ALIGN_LEFT);
			document.add(pEstInscrit);

			float[] widths = {1.5f, 7.5f};
			PdfPTable table = new PdfPTable(widths);
			table.setWidthPercentage(100f);
			table.addCell(makeCell(applicationContext.getMessage("pdf.diplome", null, Locale.getDefault())+" :", normal));
			table.addCell(makeCell(inscription.getLib_dip(), normal));
			table.addCell(makeCell(applicationContext.getMessage("pdf.year", null, Locale.getDefault())+" :", normal));
			table.addCell(makeCell(inscription.getLib_etp(), normal));
			table.addCell(makeCell(applicationContext.getMessage("pdf.composante", null, Locale.getDefault())+" :", normal));
			table.addCell(makeCell(inscription.getLib_comp(), normal));
			if(configController.isCertScolRegimeIns() && StringUtils.hasText(inscription.getLib_rge())){
				table.addCell(makeCell(applicationContext.getMessage("pdf.regime", null, Locale.getDefault())+" :", normal));
				table.addCell(makeCell(inscription.getLib_rge(), normal));
			}
			document.add(table);

			document.add(new Paragraph(" "));

			String nomSignataire = StringUtils.hasText(configController.getCertScolDescSignataire())? configController.getCertScolDescSignataire() : signataire.getQua_sig() + " " + signataire.getNom_sig();

			float[] widthsSignataire = {2f, 1.3f};
			PdfPTable tableSignataire = new PdfPTable(widthsSignataire);
			tableSignataire.setWidthPercentage(100f);
			tableSignataire.addCell(makeCellSignataire("", normal));
			tableSignataire.addCell(makeCellSignataire(applicationContext.getMessage("pdf.certificat.fait1", null, Locale.getDefault())+" " + configController.getCertScolLieuEdition() + applicationContext.getMessage("pdf.certificat.fait2", null, Locale.getDefault())+" " + date, normal));
			tableSignataire.addCell(makeCellSignataire("", normal));
			tableSignataire.addCell(makeCellSignataire(nomSignataire, normal));

			document.add(tableSignataire);

			//ajout signature
			if (signataire.getImg_sig_std() != null && signataire.getImg_sig_std().length > 0){
				LOG.debug(signataire.getImg_sig_std().toString());
				Image imageSignature = Image.getInstance(signataire.getImg_sig_std());

				int largeurSignature = configController.getDimensionPDFSignature();
				float scaleRatio = largeurSignature / imageSignature.getWidth(); 
				float newHeight = scaleRatio * imageSignature.getHeight();
				imageSignature.scaleAbsolute(largeurSignature, newHeight);
				
				// Maj 1.6.20
				// Si on doit fixer la position de la signature
				if (StringUtils.hasText(configController.getCertificatScolaritePdfPositionSignature())) {
					imageSignature.setAbsolutePosition(configController.getCertificatScolaritePdfPositionSignatureX(), configController.getCertificatScolaritePdfPositionSignatureY());
				} else {
					imageSignature.setAlignment(Element.ALIGN_RIGHT);
					imageSignature.setIndentationRight(25);
				}
				document.add(imageSignature);
			}



			// Ajout tampon
			if (StringUtils.hasText(configController.getCertScolTampon())) {
				Image imageTampon = Image.getInstance(configController.getCertScolTampon());
				
				int largeurTampon = configController.getCertScolTamponDimension();
				float scaleTampon = largeurTampon / imageTampon.getWidth();
				float newHeigthTampon = scaleTampon * imageTampon.getHeight();
				imageTampon.scaleAbsolute(largeurTampon, newHeigthTampon);
				
				if(StringUtils.hasText(configController.getCertScolTamponPosition())) {
					imageTampon.setAbsolutePosition(configController.getCertScolTamponPositionX(), configController.getCertScolTamponPositionY());
				} else {
					imageTampon.setAlignment(Element.ALIGN_RIGHT);
					imageTampon.setIndentationRight(25);
				}
				document.add(imageTampon);
			}

		} catch (BadElementException e) {
			LOG.error("Erreur à la génération du certificat : BadElementException ",e);
		} catch (MalformedURLException e) {
			LOG.error("Erreur à la génération du certificat : MalformedURLException ",e);
		} catch (IOException e) {
			LOG.error("Erreur à la génération du certificat : IOException ",e);
		} catch (DocumentException e) {
			LOG.error("Erreur à la génération du certificat : DocumentException ",e);
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
}
