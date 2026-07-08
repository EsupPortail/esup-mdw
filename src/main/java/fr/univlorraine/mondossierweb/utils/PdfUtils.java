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

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.OcspClientBouncyCastle;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfDeveloperExtension;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignature;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TSAClientBouncyCastle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.x509.util.StreamParsingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PdfUtils {

	/**
	 * outputstream size.
	 */
	private static final int OUTPUTSTREAM_SIZE = 1024;

	private static final int MIN_LENGTH_PWD = 28;

	private static final int MAX_LENGTH_PWD = 30;

	/** Taille estimée (hors jeton TSA) réservée pour le conteneur PKCS#7 de la signature */
	private static final int SIGNATURE_CONTENT_ESTIMATED = 15000;

	private static final String DIGEST_ALGORITHM = "SHA-256";

	public static ByteArrayOutputStream signPdf(PdfReader reader, boolean signatureAlt, String[] signaturePosition) {
		try {

			KeyStore ks = KeyStore.getInstance("PKCS12");
			try (InputStream ksStream = new FileInputStream(PropertyUtils.getPdfSignatureKeystorePath())) {
				ks.load(ksStream, PropertyUtils.getPdfSignaturePassword().toCharArray());
			}

			String alias = ks.aliases().nextElement();
			PrivateKey key = (PrivateKey) ks.getKey(alias, PropertyUtils.getPdfSignaturePassword().toCharArray());
			Certificate[] chain = ks.getCertificateChain(alias);

			BouncyCastleProvider provider = new BouncyCastleProvider();
			Security.addProvider(provider);

			ByteArrayOutputStream fout = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
			PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');

			PdfSignatureAppearance sap = stp.getSignatureAppearance();
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
			sap.setSignDate(Calendar.getInstance());

			if(signatureAlt && signaturePosition != null) {
				Rectangle r = new Rectangle(PropertyUtils.getPdfSignatureVisibleLlX(signaturePosition), PropertyUtils.getPdfSignatureVisibleLlY(signaturePosition),
						PropertyUtils.getPdfSignatureVisibleUrX(signaturePosition), PropertyUtils.getPdfSignatureVisibleUrY(signaturePosition));
				sap.setVisibleSignature(r, reader.getNumberOfPages(), null);
			}

			// Dictionnaire de la signature détachée PKCS#7
			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
			dic.setDate(new PdfDate(sap.getSignDate()));
			if(PropertyUtils.getPdfSignatureReason() != null) {
				dic.setReason(PropertyUtils.getPdfSignatureReason());
			}
			if(PropertyUtils.getPdfSignatureLocation() != null) {
				dic.setLocation(PropertyUtils.getPdfSignatureLocation());
			}
			sap.setCryptoDictionary(dic);

			// Compatibilité ALT : LTV (DSS) + horodatage TSA
			TSAClientBouncyCastle tsc = null;
			if(signatureAlt) {
				// Ajout des informations de validation (OCSP/CRL) dans le DSS (niveau PAdES B-LT)
				AdobeLtvEnabling adobeLtvEnabling = new AdobeLtvEnabling(stp);
				adobeLtvEnabling.enable(chain);

				// TSAClient (Timestamping Authority)
				String tsa_url = PropertyUtils.getPdfSignatureTsaUrl();
				String tsa_username = PropertyUtils.getPdfSignatureTsaUsername();
				String tsa_password = PropertyUtils.getPdfSignatureTsaPassword();
				int tsa_tokensize = PropertyUtils.getPdfSignatureTsaTokenSize();
				tsc = new TSAClientBouncyCastle(tsa_url, tsa_username, tsa_password, tsa_tokensize);
				tsc.setDigestName(DIGEST_ALGORITHM);
			}

			// Réservation de l'espace pour la signature
			int contentEstimated = SIGNATURE_CONTENT_ESTIMATED + (tsc != null ? tsc.getTokenSizeEstimate() : 0);
			Map<PdfName, Integer> exc = new HashMap<>();
			exc.put(PdfName.CONTENTS, contentEstimated * 2 + 2);
			sap.preClose(exc);

			// Calcul de l'empreinte du document
			MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
			byte[] buf = new byte[8192];
			int n;
			try (InputStream data = sap.getRangeStream()) {
				while ((n = data.read(buf)) > 0) {
					md.update(buf, 0, n);
				}
			}
			byte[] hash = md.digest();
			Calendar cal = sap.getSignDate();

			// Construction du conteneur PKCS#7 (avec jeton TSA le cas échéant)
			PdfPKCS7 sgn = new PdfPKCS7(key, chain, null, DIGEST_ALGORITHM, provider.getName(), false);
			byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal, null);
			sgn.update(sh, 0, sh.length);
			byte[] encodedSig = sgn.getEncodedPKCS7(hash, cal, tsc, null);

			if (contentEstimated + 2 < encodedSig.length) {
				throw new DocumentException("La signature ne tient pas dans l'espace réservé (" + encodedSig.length + " > " + contentEstimated + ")");
			}
			byte[] paddedSig = new byte[contentEstimated];
			System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);

			PdfDictionary dic2 = new PdfDictionary();
			dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
			sap.close(dic2);

			fout.close();
			return fout;


		} catch (Exception e) {
			log.error("Erreur lors de la signature du PDF ",e);
		}

		return null;
	}


	public static byte[] generatePwd() {
		String pwd = RandomStringUtils.randomAlphanumeric(MIN_LENGTH_PWD, MAX_LENGTH_PWD);
		log.debug("ownerPwd : {}", pwd);
		return pwd.getBytes();
	}

	public static class AdobeLtvEnabling {
		/**
		 * Use this constructor with a {@link PdfStamper} in append mode. Otherwise
		 * the existing signatures will be damaged.
		 */
		public AdobeLtvEnabling(PdfStamper pdfStamper) {
			this.pdfStamper = pdfStamper;
		}

		/**
		 * Call this method to have LTV information added to the {@link PdfStamper}
		 * given in the constructor. The signing certificate chain is used to add
		 * validation data of the future signature.
		 */
		public void enable(Certificate[] signingChain) throws java.security.GeneralSecurityException, IOException, StreamParsingException, OCSPException, OperatorCreationException {
			AcroFields fields = pdfStamper.getAcroFields();
			boolean encrypted = pdfStamper.getReader().isEncrypted();

			// Signatures déjà présentes dans le document
			for (String name : fields.getSignatureTypes().keySet()) {
				PdfDictionary signatureDictionary = fields.getSignatureDictionary(name);
				if (signatureDictionary == null) {
					continue;
				}
				PdfPKCS7 pkcs7 = fields.verifySignature(name);
				if (pkcs7 != null && pkcs7.getSigningCertificate() != null) {
					addLtvForChain(pkcs7.getSigningCertificate(), getSignatureHashKey(signatureDictionary, encrypted));
				}
			}

			// Chaîne du certificat signataire (la signature n'est apposée qu'ensuite)
			if (signingChain != null && signingChain.length > 0 && signingChain[0] instanceof X509Certificate signingCert) {
				addLtvForChain(signingCert, getSignatureHashKey(signingCert));
			}

			outputDss();
		}

		//
		// the actual LTV enabling methods
		//
		void addLtvForChain(X509Certificate certificate, PdfName key) throws java.security.GeneralSecurityException, IOException, StreamParsingException, OperatorCreationException, OCSPException {
			ValidationData validationData = new ValidationData();

			while (certificate != null) {
				log.debug("Certificate name : {}" , certificate.getSubjectX500Principal().getName());
				X509Certificate issuer = getIssuerCertificate(certificate);
				validationData.certs.add(certificate.getEncoded());
				byte[] ocspResponse = getOcspResponse(certificate, issuer);
				if (ocspResponse != null) {
					log.debug("  with OCSP response");
					validationData.ocsps.add(ocspResponse);
					X509Certificate ocspSigner = getOcspSignerCertificate(ocspResponse);
					if (ocspSigner != null) {
						log.debug("  signed by {}\n", ocspSigner.getSubjectX500Principal().getName());
					}
					addLtvForChain(ocspSigner, getOcspHashKey(ocspResponse));
				} else {
					Collection<byte[]> crl = getCrl(certificate);
					if (crl != null && !crl.isEmpty()) {
						log.debug("  with {} CRLs\n", crl.size());
						validationData.crls.addAll(crl);
						for (byte[] crlBytes : crl) {
							addLtvForChain(null, getCrlHashKey(crlBytes));
						}
					}
				}
				certificate = issuer;
			}

			validated.put(key, validationData);
		}

		void outputDss() throws IOException {
			PdfWriter writer = pdfStamper.getWriter();
			PdfReader reader = pdfStamper.getReader();

			PdfDictionary dss = new PdfDictionary();
			PdfDictionary vrim = new PdfDictionary();
			PdfArray ocsps = new PdfArray();
			PdfArray crls = new PdfArray();
			PdfArray certs = new PdfArray();

			writer.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ESIC, PdfWriter.PDF_VERSION_1_7, 5));
			writer.addDeveloperExtension(new PdfDeveloperExtension(PdfName.ADBE, new PdfName("1.7"), 8));

			PdfDictionary catalog = reader.getCatalog();
			pdfStamper.markUsed(catalog);
			for (PdfName vkey : validated.keySet()) {
				PdfArray ocsp = new PdfArray();
				PdfArray crl = new PdfArray();
				PdfArray cert = new PdfArray();
				PdfDictionary vri = new PdfDictionary();
				for (byte[] b : validated.get(vkey).crls) {
					PdfStream ps = new PdfStream(b);
					ps.flateCompress();
					PdfIndirectReference iref = writer.addToBody(ps, false).getIndirectReference();
					crl.add(iref);
					crls.add(iref);
				}
				for (byte[] b : validated.get(vkey).ocsps) {
					b = buildOCSPResponse(b);
					PdfStream ps = new PdfStream(b);
					ps.flateCompress();
					PdfIndirectReference iref = writer.addToBody(ps, false).getIndirectReference();
					ocsp.add(iref);
					ocsps.add(iref);
				}
				for (byte[] b : validated.get(vkey).certs) {
					PdfStream ps = new PdfStream(b);
					ps.flateCompress();
					PdfIndirectReference iref = writer.addToBody(ps, false).getIndirectReference();
					cert.add(iref);
					certs.add(iref);
				}
				if (!ocsp.isEmpty())
					vri.put(new PdfName("OCSP"), writer.addToBody(ocsp, false).getIndirectReference());
				if (!crl.isEmpty())
					vri.put(new PdfName("CRL"), writer.addToBody(crl, false).getIndirectReference());
				if (!cert.isEmpty())
					vri.put(PdfName.CERT, writer.addToBody(cert, false).getIndirectReference());
				vri.put(PdfName.TU, new PdfDate());
				vrim.put(vkey, writer.addToBody(vri, false).getIndirectReference());
			}
			dss.put(PdfName.VRI, writer.addToBody(vrim, false).getIndirectReference());
			if (!ocsps.isEmpty())
				dss.put(PdfName.OCSPS, writer.addToBody(ocsps, false).getIndirectReference());
			if (!crls.isEmpty())
				dss.put(PdfName.CRLS, writer.addToBody(crls, false).getIndirectReference());
			if (!certs.isEmpty())
				dss.put(PdfName.CERTS, writer.addToBody(certs, false).getIndirectReference());
			catalog.put(PdfName.DSS, writer.addToBody(dss, false).getIndirectReference());
		}

		//
		// OCSP and CRL clients
		//
		static byte[] getOcspResponse(X509Certificate certificate, X509Certificate issuer) {
			if (certificate == null || issuer == null) {
				return null;
			}
			String url = PdfPKCS7.getOCSPURL(certificate);
			if (url == null || url.isEmpty()) {
				return null;
			}
			return new OcspClientBouncyCastle(certificate, issuer, url).getEncoded();
		}

		static Collection<byte[]> getCrl(X509Certificate certificate) throws IOException {
			List<byte[]> crls = new ArrayList<>();
			if (certificate == null) {
				return crls;
			}
			ASN1Primitive obj = getExtensionValue(certificate, Extension.cRLDistributionPoints.getId());
			if (obj == null) {
				return crls;
			}
			CRLDistPoint dist = CRLDistPoint.getInstance(obj);
			for (DistributionPoint p : dist.getDistributionPoints()) {
				DistributionPointName distributionPointName = p.getDistributionPoint();
				if (distributionPointName == null || DistributionPointName.FULL_NAME != distributionPointName.getType()) {
					continue;
				}
				GeneralNames generalNames = (GeneralNames) distributionPointName.getName();
				for (GeneralName generalName : generalNames.getNames()) {
					if (generalName.getTagNo() != GeneralName.uniformResourceIdentifier) {
						continue;
					}
					String url = ASN1IA5String.getInstance(generalName.getName()).getString();
					try {
						byte[] crl = download(url);
						if (crl != null && crl.length > 0) {
							crls.add(crl);
						}
					} catch (IOException e) {
						log.warn("Impossible de télécharger la CRL {}", url, e);
					}
				}
			}
			return crls;
		}

		static byte[] download(String url) throws IOException {
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			if (con.getResponseCode() / 100 != 2) {
				throw new IOException(MessageLocalization.getComposedMessage("invalid.http.response.1", con.getResponseCode()));
			}
			try (InputStream inp = (InputStream) con.getContent()) {
				byte[] buf = new byte[1024];
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				while (true) {
					int n = inp.read(buf, 0, buf.length);
					if (n <= 0)
						break;
					bout.write(buf, 0, n);
				}
				return bout.toByteArray();
			}
		}

		//
		// VRI signature hash key calculation
		//
		static PdfName getCrlHashKey(byte[] crlBytes) throws NoSuchAlgorithmException, IOException, CRLException, CertificateException {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509CRL crl = (X509CRL)cf.generateCRL(new ByteArrayInputStream(crlBytes));
			byte[] signatureBytes = crl.getSignature();
			DEROctetString octetString = new DEROctetString(signatureBytes);
			byte[] octetBytes = octetString.getEncoded();
			byte[] octetHash = hashBytesSha256(octetBytes);
			return new PdfName(convertToHex(octetHash));
		}

		static PdfName getOcspHashKey(byte[] basicResponseBytes) throws NoSuchAlgorithmException, IOException {
			BasicOCSPResponse basicResponse = BasicOCSPResponse.getInstance(basicResponseBytes);
			byte[] signatureBytes = basicResponse.getSignature().getBytes();
			DEROctetString octetString = new DEROctetString(signatureBytes);
			byte[] octetBytes = octetString.getEncoded();
			byte[] octetHash = hashBytesSha256(octetBytes);
			return new PdfName(convertToHex(octetHash));
		}

		static PdfName getSignatureHashKey(PdfDictionary dic, boolean encrypted) throws NoSuchAlgorithmException, IOException {
			PdfString contents = dic.getAsString(PdfName.CONTENTS);
			byte[] bc = contents.getOriginalBytes();
			if (new PdfName("ETSI.RFC3161").equals(PdfReader.getPdfObject(dic.get(PdfName.SUBFILTER)))) {
				try (   ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(bc)) ) {
					ASN1Primitive pkcs = din.readObject();
					bc = pkcs.getEncoded();
				}
			}
			byte[] bt = hashBytesSha256(bc);
			return new PdfName(convertToHex(bt));
		}

		static PdfName getSignatureHashKey(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateException {
			byte[] bt = hashBytesSha256(certificate.getEncoded());
			return new PdfName(convertToHex(bt));
		}

		static byte[] hashBytesSha256(byte[] b) throws NoSuchAlgorithmException {
			return MessageDigest.getInstance("SHA-256").digest(b);
		}

		private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

		static String convertToHex(byte[] bytes) {
			StringBuilder sb = new StringBuilder(bytes.length * 2);
			for (byte b : bytes) {
				sb.append(HEX_DIGITS[(b >> 4) & 0x0f]).append(HEX_DIGITS[b & 0x0f]);
			}
			return sb.toString();
		}

		//
		// OCSP response helpers
		//
		static X509Certificate getOcspSignerCertificate(byte[] basicResponseBytes) throws CertificateException, OCSPException, OperatorCreationException {
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
			BasicOCSPResponse borRaw = BasicOCSPResponse.getInstance(basicResponseBytes);
			BasicOCSPResp bor = new BasicOCSPResp(borRaw);

			for (final X509CertificateHolder x509CertificateHolder : bor.getCerts()) {
				X509Certificate x509Certificate = converter.getCertificate(x509CertificateHolder);

				JcaContentVerifierProviderBuilder jcaContentVerifierProviderBuilder = new JcaContentVerifierProviderBuilder();
				jcaContentVerifierProviderBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
				final PublicKey publicKey = x509Certificate.getPublicKey();
				ContentVerifierProvider contentVerifierProvider = jcaContentVerifierProviderBuilder.build(publicKey);

				if (bor.isSignatureValid(contentVerifierProvider))
					return x509Certificate;
			}

			return null;
		}

		static byte[] buildOCSPResponse(byte[] BasicOCSPResponse) throws IOException {
			DEROctetString doctet = new DEROctetString(BasicOCSPResponse);
			ASN1EncodableVector v2 = new ASN1EncodableVector();
			v2.add(OCSPObjectIdentifiers.id_pkix_ocsp_basic);
			v2.add(doctet);
			ASN1Enumerated den = new ASN1Enumerated(0);
			ASN1EncodableVector v3 = new ASN1EncodableVector();
			v3.add(den);
			v3.add(new DERTaggedObject(true, 0, new DERSequence(v2)));
			DERSequence seq = new DERSequence(v3);
			return seq.getEncoded();
		}

		//
		// X509 certificate related helpers
		//
		static X509Certificate getIssuerCertificate(X509Certificate certificate) throws IOException, StreamParsingException {
			String url = getCACURL(certificate);
			if (url != null && !url.isEmpty()) {
				byte[] cert = download(url);

				X509CertParser parser = new X509CertParser();
				parser.engineInit(new ByteArrayInputStream(cert));
				return (X509Certificate) parser.engineRead();

			}
			return null;
		}

		static String getCACURL(X509Certificate certificate) {
			ASN1Primitive obj;
			try {
				obj = getExtensionValue(certificate, Extension.authorityInfoAccess.getId());
				if (obj == null) {
					return null;
				}
				ASN1Sequence AccessDescriptions = (ASN1Sequence) obj;
				for (int i = 0; i < AccessDescriptions.size(); i++) {
					ASN1Sequence AccessDescription = (ASN1Sequence) AccessDescriptions.getObjectAt(i);
					if ( AccessDescription.size() != 2 ) {
						continue;
					}
					else if (AccessDescription.getObjectAt(0) instanceof ASN1ObjectIdentifier) {
						ASN1ObjectIdentifier id = (ASN1ObjectIdentifier)AccessDescription.getObjectAt(0);
						if ("1.3.6.1.5.5.7.48.2".equals(id.getId())) {
							ASN1Primitive description = (ASN1Primitive)AccessDescription.getObjectAt(1);
							String AccessLocation =  getStringFromGeneralName(description);
							if (AccessLocation == null) {
								return "" ;
							}
							else {
								return AccessLocation ;
							}
						}
					}
				}
			} catch (IOException e) {
				return null;
			}
			return null;
		}

		static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
			byte[] bytes = certificate.getExtensionValue(oid);
			if (bytes == null) {
				return null;
			}
			ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
			ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
			aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
			return aIn.readObject();
		}

		private static String getStringFromGeneralName(ASN1Primitive names) throws IOException {
			ASN1TaggedObject taggedObject = (ASN1TaggedObject) names ;
			return new String(ASN1OctetString.getInstance(taggedObject, false).getOctets(), "ISO-8859-1");
		}

		//
		// inner classes
		//
		static class ValidationData {
			final List<byte[]> crls = new ArrayList<byte[]>();
			final List<byte[]> ocsps = new ArrayList<byte[]>();
			final List<byte[]> certs = new ArrayList<byte[]>();
		}

		//
		// member variables
		//
		final PdfStamper pdfStamper;

		final Map<PdfName,ValidationData> validated = new HashMap<PdfName,ValidationData>();
	}

}
