package fr.univlorraine.mondossierweb.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;

public class PdfUtils {
	
	private static Logger LOG = LoggerFactory.getLogger(PdfUtils.class);
	
	/**
	 * outputstream size.
	 */
	private static final int OUTPUTSTREAM_SIZE = 1024;

	private static final int MIN_LENGTH_PWD = 28;

	private static final int MAX_LENGTH_PWD = 30;

	public static ByteArrayOutputStream signPdf(PdfReader reader) {
		try {

			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(new FileInputStream(PropertyUtils.getPdfSignatureKeystorePath()), PropertyUtils.getPdfSignaturePassword().toCharArray());

			String alias = (String)ks.aliases().nextElement();
			PrivateKey key = (PrivateKey)ks.getKey(alias, PropertyUtils.getPdfSignaturePassword().toCharArray());
			Certificate[] chain = ks.getCertificateChain(alias);

			ByteArrayOutputStream fout = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
			PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');

			PdfSignatureAppearance sap = stp.getSignatureAppearance();
			sap.setCrypto(key, chain, null, PdfSignatureAppearance.SELF_SIGNED);
			if(PropertyUtils.getPdfSignatureProvider() != null) {
				sap.setProvider(PropertyUtils.getPdfSignatureProvider());
			}
			if(PropertyUtils.getPdfSignatureReason() != null) {
				sap.setReason(PropertyUtils.getPdfSignatureReason());
			}
			if(PropertyUtils.getPdfSignatureLocation() != null) {
				sap.setLocation(PropertyUtils.getPdfSignatureLocation());
			}
			if(PropertyUtils.getPdfSignatureContact() != null) {
				sap.setContact(PropertyUtils.getPdfSignatureContact());
			}
			sap.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
			//sap.setVisibleSignature(new Rectangle(100, 100, 200, 200), reader.getNumberOfPages(), null);

			stp.close();
			fout.close();
			return fout;

		} catch (Exception e) {
			LOG.error("Erreur lors de la signature du PDF ",e);
		}

		return null;
	}

	public static byte[] generatePwd() {
		String pwd = RandomStringUtils.randomAlphanumeric(MIN_LENGTH_PWD, MAX_LENGTH_PWD);
		LOG.debug("ownerPwd :" + pwd);
		return pwd.getBytes();
	}
	
}
