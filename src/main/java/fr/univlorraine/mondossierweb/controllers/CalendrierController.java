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

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.PdfUtils;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Gestion du calendrier des examens
 */
@Component
public class CalendrierController {

	private Logger LOG = LoggerFactory.getLogger(CalendrierController.class);


	/**
	 * outputstream size.
	 */
	private static final int OUTPUTSTREAM_SIZE = 1024;
	/**
	 * marge.
	 */
	private static final float MARGE_PDF = 1.0f;
	/**
	 * l'écartement du pied de page (libellé de la promo et date d'édition) des pdf.
	 */
	private static final int ECARTEMENT_PIED_PAGE_PDF = 120;


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
	 * @return le fichier pdf du calendrier des examens.
	 */
	public com.vaadin.server.Resource exportPdf() {


		String nomFichier = applicationContext.getMessage("pdf.calendrier.title", null, Locale.getDefault())+" " + MainUI.getCurrent().getEtudiant().getNom().replace('.', ' ').replace(' ', '_') + ".pdf";

		nomFichier = nomFichier.replaceAll(" ","_");

		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getStream() {
				try {
					ByteArrayOutputStream baosPDF = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
					PdfWriter docWriter = null;
					Document document = configureDocument(MARGE_PDF);
					docWriter = PdfWriter.getInstance(document, baosPDF);
					// Test si on doit activer l'encryption
					byte[] ownerPwd = PdfUtils.generatePwd();
					if(PropertyUtils.isEnablePdfSecurity()){
						docWriter.setEncryption(null, ownerPwd, PdfWriter.AllowPrinting, PdfWriter.ENCRYPTION_AES_128);
					}
					docWriter.setStrictImageSequence(true);
					creerPdfCalendrier(document,MainUI.getCurrent().getEtudiant());
					docWriter.close();
					baosPDF.close();
					if(PropertyUtils.isEnablePdfCalendrierSignature()) {
						//Creation de l'export après ajout de signature
						return new ByteArrayInputStream(PdfUtils.signPdf(new PdfReader(baosPDF.toByteArray(), ownerPwd)).toByteArray());
					} else {
						//Creation de l'export
						return new ByteArrayInputStream(baosPDF.toByteArray());
					}
				} catch (DocumentException e) {
					LOG.error("Erreur à la génération du calendrier des examens : DocumentException ",e);
					return null;
				} catch (IOException e) {
					LOG.error("Erreur à la génération du calendrier des examens : IOException ",e);
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
	public void creerPdfCalendrier(final Document document, Etudiant etudiant) {



		//configuration des fonts
		Font normal = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, Font.NORMAL);
		Font normalbig = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.BOLD);
		Font legerita = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.ITALIC);
		Font headerbig = FontFactory.getFont(FontFactory.TIMES_ROMAN, 16, Font.BOLD);
		Font header = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.BOLD);

		//pieds de pages:
		Date d = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String date = dateFormat.format(d);
		//alignement des libellés du pied de page:
		String partie1 = applicationContext.getMessage("pdf.calendrier.title", null, Locale.getDefault());
		String partie2 = applicationContext.getMessage("pdf.edition.date", null, Locale.getDefault())+" : " + date;
		if (partie1.length() < ECARTEMENT_PIED_PAGE_PDF) {
			int diff = ECARTEMENT_PIED_PAGE_PDF - partie1.length();
			for (int i = 0; i < diff; i++) {
				partie1 = partie1 + " ";

			}
		} 
		if (partie2.length() < ECARTEMENT_PIED_PAGE_PDF) {
			int diff = ECARTEMENT_PIED_PAGE_PDF - partie2.length();
			for (int i = 0; i < diff; i++) {
				partie2 = " " + partie2;
			}
		}

		//création du pied de page:
		Phrase phra = new Phrase(partie1 + " -"+applicationContext.getMessage("pdf.page", null, Locale.getDefault()), legerita);
		Phrase phra2 = new Phrase("- "+partie2, legerita);
		HeaderFooter hf = new HeaderFooter(phra, phra2);
		hf.setAlignment(HeaderFooter.ALIGN_CENTER);
		document.setFooter(hf);	 


		//ouverte du document.
		document.open();
		try {
			//ajout image test
			if (StringUtils.hasText(configController.getLogoUniversitePdf())){
				Image imageLogo = Image.getInstance(configController.getLogoUniversitePdf());

				int largeurLogo = configController.getLogoUniversitePdfDimension();
				float scaleRatio = largeurLogo / imageLogo.getWidth(); 
				float newHeight = scaleRatio * imageLogo.getHeight();
				imageLogo.scaleAbsolute(largeurLogo, newHeight);
				
				imageLogo.setAbsolutePosition(configController.getLogoUniversitePdfPaysagePositionX() - largeurLogo, configController.getLogoUniversitePdfPaysagePositionY());
				document.add(imageLogo);
			}



			//nouveau paragraphe
			Paragraph p = new Paragraph(applicationContext.getMessage("pdf.calendrier.title", null, Locale.getDefault()).toUpperCase()+"\n\n", headerbig);
			p.setIndentationLeft(15);
			document.add(p);

			if (etudiant.getNom() != null) {
				Paragraph p0 = new Paragraph(etudiant.getNom(), normal);
				p0.setIndentationLeft(15);
				document.add(p0);
			}
			if (etudiant.getCod_etu() != null) {
				Paragraph p01 = new Paragraph(applicationContext.getMessage("pdf.folder", null, Locale.getDefault())+" : " + etudiant.getCod_etu(), normal);
				p01.setIndentationLeft(15);
				document.add(p01);
			}
			if (etudiant.getCod_nne() != null) {
				Paragraph p02 = new Paragraph(applicationContext.getMessage("pdf.nne", null, Locale.getDefault())+" : " + etudiant.getCod_nne(), normal);
				p02.setIndentationLeft(15);
				document.add(p02);
			}
			if (etudiant.getEmail() != null) {
				Paragraph p03 = new Paragraph(applicationContext.getMessage("pdf.mail", null, Locale.getDefault()) + " : " + etudiant.getEmail(), normal);
				p03.setIndentationLeft(15);
				document.add(p03);
			}

			Paragraph p03 = new Paragraph(applicationContext.getMessage("pdf.edition.date", null, Locale.getDefault()) + " : " + date, normal);
			p03.setIndentationLeft(15);
			document.add(p03);
			document.add(new Paragraph("\n"));




			//Partie Calendrier
			PdfPTable table = new PdfPTable(1);
			table.setWidthPercentage(98);
			PdfPCell cell = new PdfPCell(new Paragraph(applicationContext.getMessage("pdf.calendrier.subtitle", null, Locale.getDefault()).toUpperCase()+" ", header));
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setBackgroundColor(new Color(153, 153, 255));
			table.addCell(cell);



			PdfPTable table2;

			boolean affNumPlaceExamen = configController.isAffNumPlaceExamen();

			boolean affDetailExamen = configController.isAffDetailExamen();

			if(affNumPlaceExamen) {
				table2 = new PdfPTable(7);
				table2.setWidthPercentage(98);
				int [] tabWidth = {15,10,10,40,30,10,60};
				table2.setWidths(tabWidth);
				if (affDetailExamen) {
					table2 = new PdfPTable(11);
					table2.setWidthPercentage(98);
					tabWidth = new int[] {17,13,15,25,25,7,60,25,40,20,15};
					table2.setWidths(tabWidth);
				}
			}else{
				table2 = new PdfPTable(6);
				table2.setWidthPercentage(98);
				int [] tabWidth = {15,10,10,45,30,65};
				table2.setWidths(tabWidth);
				if (affDetailExamen) {
					table2 = new PdfPTable(10);
					table2.setWidthPercentage(98);
					tabWidth = new int[] {15,7,10,25,25,63,25,40,25,30};
					table2.setWidths(tabWidth);
				}
			}

			Paragraph p1 = new Paragraph(applicationContext.getMessage("pdf.date", null, Locale.getDefault()),normalbig);
			Paragraph p2 = new Paragraph(applicationContext.getMessage("pdf.heure", null, Locale.getDefault()),normalbig);
			Paragraph p3 = new Paragraph(applicationContext.getMessage("pdf.duree", null, Locale.getDefault()),normalbig);
			Paragraph p4 = new Paragraph(applicationContext.getMessage("pdf.batiment", null, Locale.getDefault()),normalbig);
			Paragraph p5 = new Paragraph(applicationContext.getMessage("pdf.salle", null, Locale.getDefault()),normalbig);
			Paragraph p6 = new Paragraph(applicationContext.getMessage("pdf.place", null, Locale.getDefault()),normalbig);
			Paragraph p7 = new Paragraph(applicationContext.getMessage("pdf.examen", null, Locale.getDefault()),normalbig);
			Paragraph p8 = new Paragraph(applicationContext.getMessage("pdf.codeepreuve", null, Locale.getDefault()),normalbig);
			Paragraph p9 = new Paragraph(applicationContext.getMessage("pdf.libsession", null, Locale.getDefault()),normalbig);
			Paragraph p10 = new Paragraph(applicationContext.getMessage("pdf.codeetape", null, Locale.getDefault()),normalbig);
			Paragraph p11 = new Paragraph(applicationContext.getMessage("pdf.versionetape", null, Locale.getDefault()),normalbig);

			PdfPCell ct1 = new PdfPCell(p1);
			PdfPCell ct2 = new PdfPCell(p2);
			PdfPCell ct3 = new PdfPCell(p3);
			PdfPCell ct4 = new PdfPCell(p4);
			PdfPCell ct5 = new PdfPCell(p5);
			PdfPCell ct6 =  new PdfPCell(p6);
			PdfPCell ct7 = new PdfPCell(p7);
			PdfPCell ct8 = new PdfPCell(p8);
			PdfPCell ct9 = new PdfPCell(p9);
			PdfPCell ct10 = new PdfPCell(p10);
			PdfPCell ct11 = new PdfPCell(p11);

			ct1.setBorder(Rectangle.BOTTOM); ct1.setBorderColorBottom(Color.black);
			ct2.setBorder(Rectangle.BOTTOM); ct2.setBorderColorBottom(Color.black);
			ct3.setBorder(Rectangle.BOTTOM); ct2.setBorderColorBottom(Color.black);
			ct4.setBorder(Rectangle.BOTTOM); ct1.setBorderColorBottom(Color.black);
			ct5.setBorder(Rectangle.BOTTOM); ct2.setBorderColorBottom(Color.black);
			ct6.setBorder(Rectangle.BOTTOM); ct2.setBorderColorBottom(Color.black);
			ct7.setBorder(Rectangle.BOTTOM); ct2.setBorderColorBottom(Color.black);
			ct8.setBorder(Rectangle.BOTTOM); ct2.setBorderColorBottom(Color.black);
			ct9.setBorder(Rectangle.BOTTOM); ct1.setBorderColorBottom(Color.black);
			ct10.setBorder(Rectangle.BOTTOM); ct1.setBorderColorBottom(Color.black);
			ct11.setBorder(Rectangle.BOTTOM); ct1.setBorderColorBottom(Color.black);

			table2.addCell(ct1);
			table2.addCell(ct2);
			table2.addCell(ct3);
			table2.addCell(ct4);
			table2.addCell(ct5);
			if(affNumPlaceExamen)
				table2.addCell(ct6);
			table2.addCell(ct7);
			if(affDetailExamen) {
				table2.addCell(ct8);
				table2.addCell(ct9);
				table2.addCell(ct10);
				table2.addCell(ct11);
			}




			for (int i = 0; i < etudiant.getCalendrier().size(); i++) {
				Paragraph pa = new Paragraph(Utils.formatDateToString(etudiant.getCalendrier().get(i).getDatedeb()), normal);
				PdfPCell celltext = new PdfPCell(pa);
				celltext.setBorder(Rectangle.NO_BORDER);

				Paragraph pa2 = new Paragraph(etudiant.getCalendrier().get(i).getHeure(), normal);
				PdfPCell celltext2 = new PdfPCell(pa2);
				celltext2.setBorder(Rectangle.NO_BORDER);

				Paragraph pa3 = new Paragraph(etudiant.getCalendrier().get(i).getDuree(), normal);
				PdfPCell celltext3 = new PdfPCell(pa3);
				celltext3.setBorder(Rectangle.NO_BORDER);

				Paragraph pa4 = new Paragraph(etudiant.getCalendrier().get(i).getBatiment(), normal);
				PdfPCell celltext4 = new PdfPCell(pa4);
				celltext4.setBorder(Rectangle.NO_BORDER);

				Paragraph pa5 = new Paragraph(etudiant.getCalendrier().get(i).getSalle(), normal);
				PdfPCell celltext5 = new PdfPCell(pa5);
				celltext5.setBorder(Rectangle.NO_BORDER);

				Paragraph pa6 = new Paragraph(etudiant.getCalendrier().get(i).getPlace(), normal);
				PdfPCell celltext6 = new PdfPCell(pa6);
				celltext6.setBorder(Rectangle.NO_BORDER);

				Paragraph pa7 = new Paragraph(etudiant.getCalendrier().get(i).getEpreuve(), normal);
				PdfPCell celltext7 = new PdfPCell(pa7);
				celltext7.setBorder(Rectangle.NO_BORDER);

				Paragraph pa8 = new Paragraph(etudiant.getCalendrier().get(i).getCodeepreuve(), normal);
				PdfPCell celltext8 = new PdfPCell(pa8);
				celltext8.setBorder(Rectangle.NO_BORDER);

				Paragraph pa9 = new Paragraph(etudiant.getCalendrier().get(i).getLibsession(), normal);
				PdfPCell celltext9 = new PdfPCell(pa9);
				celltext9.setBorder(Rectangle.NO_BORDER);

				Paragraph pa10 = new Paragraph(etudiant.getCalendrier().get(i).getCodeetape(), normal);
				PdfPCell celltext10 = new PdfPCell(pa10);
				celltext10.setBorder(Rectangle.NO_BORDER);

				Paragraph pa11 = new Paragraph(etudiant.getCalendrier().get(i).getVersionetape(), normal);
				PdfPCell celltext11 = new PdfPCell(pa11);
				celltext11.setBorder(Rectangle.NO_BORDER);

				table2.addCell(celltext);
				table2.addCell(celltext2);
				table2.addCell(celltext3);
				table2.addCell(celltext4);
				table2.addCell(celltext5);
				if(affNumPlaceExamen)
					table2.addCell(celltext6);
				table2.addCell(celltext7);
				if(affDetailExamen) {
					table2.addCell(celltext8);
					table2.addCell(celltext9);
					table2.addCell(celltext10);
					table2.addCell(celltext11);
				}

				/*PdfPCell celltext4 = new PdfPCell(table3);
				celltext4.setBorder(Rectangle.NO_BORDER);
				table2.addCell(celltext4);*/

			}
			document.add(table);
			document.add(table2);
			document.add(new Paragraph("\n"));







		} catch (BadElementException e) {
			LOG.error("Erreur à la génération du calendrier des examens : BadElementException ",e);
		} catch (MalformedURLException e) {
			LOG.error("Erreur à la génération du calendrier des examens : MalformedURLException ",e);
		} catch (IOException e) {
			LOG.error("Erreur à la génération du calendrier des examens : IOException ",e);
		} catch (DocumentException e) {
			LOG.error("Erreur à la génération du calendrier des examens : DocumentException ",e);
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
	private Document configureDocument(final float margin) {

		Document document = new Document();

		document.setPageSize(PageSize.A4.rotate());
		float marginPage = (margin / 2.54f) * 72f;
		document.setMargins(marginPage, marginPage, marginPage, marginPage);

		return document;
	}


}
