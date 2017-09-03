package org.idstack.validator;

import com.google.gson.Gson;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;
import org.idstack.feature.document.Extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Chandu Herath
 * @date 3/9/2017
 * @since 1.0
 */

public class JsonExtractor {

    public static final String SIGNATUREALGO = "SHA256withRSA";
    public static String PKCS12FILE;
    public static char[] PKCS12PASSWORD;
    public static String PUBLICCERURL;

    private Parser jsonParser = new Parser();

    public JsonExtractor(String privateCertFilePath, String password, String publicCertURL) {
        this.PKCS12FILE = privateCertFilePath;
        this.PKCS12PASSWORD = password.toCharArray();
        this.PUBLICCERURL = publicCertURL;
    }

    public String signExtactedJson(String jsonString) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CMSException, OperatorCreationException, NoSuchProviderException, IOException {
        Document digitizedDocument = jsonParser.parseDocumentJson(jsonString);
        digitizedDocument = sign(digitizedDocument);
        return new Gson().toJson(digitizedDocument);
    }

    private Document sign(Document completeDigitalJson) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, IOException, UnrecoverableKeyException, CMSException, OperatorCreationException {

        String jsonStringToSign = new Gson().toJson(completeDigitalJson.getContent());

        //Setting BouncyCastle as the security provoder
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        //Load the .pfx file into a key store
        KeyStore keyStore = this.loadKeyStore(provider);

        CMSSignedDataGenerator signatureGenerator = this.setUpProvider(keyStore);
        byte[] signedBytes = this.signMessagePKCS7(jsonStringToSign.getBytes("UTF-8"), signatureGenerator);
        String signedString = new String(Base64.encode(signedBytes));

        String signerID = generateUniqueID();//generate signature ID
        org.idstack.feature.document.Signature signature = new org.idstack.feature.document.Signature(signedString, PUBLICCERURL);
        Extractor extractor = new Extractor(signerID, signature);
        completeDigitalJson.setExtractor(extractor);
        return completeDigitalJson;
    }

    private KeyStore loadKeyStore(BouncyCastleProvider provider) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyStore ks = KeyStore.getInstance("PKCS12", provider.getName());
        ks.load(new FileInputStream(getClass().getClassLoader().getResource(PKCS12FILE).getFile()), PKCS12PASSWORD);
        return ks;
    }

    @SuppressWarnings("Duplicates")
    private CMSSignedDataGenerator setUpProvider(final KeyStore keyStore) throws KeyStoreException, CertificateEncodingException,
            UnrecoverableKeyException, NoSuchAlgorithmException, OperatorCreationException, CMSException {
        String alias = keyStore.aliases().nextElement();
        java.security.cert.Certificate[] certChain = keyStore.getCertificateChain(alias);

        final List<java.security.cert.Certificate> certList = new ArrayList<>();
        for (int i = 0, length = certChain == null ? 0 : certChain.length; i < length; i++) {
            certList.add(certChain[i]);
        }

        Store certStore = new JcaCertStore(certList);//Storing certificates for later lookups

        java.security.cert.Certificate cert = keyStore.getCertificate(alias);

        ContentSigner signer = new JcaContentSignerBuilder(SIGNATUREALGO).setProvider("BC").build((PrivateKey) keyStore.
                getKey(alias, PKCS12PASSWORD));
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().
                setProvider("BC").build()).build(signer, (X509Certificate) cert));
        generator.addCertificates(certStore);

        return generator;

    }

    private byte[] signMessagePKCS7(final byte[] content, final CMSSignedDataGenerator generator) throws CMSException,
            IOException {
        CMSTypedData cmsdata = new CMSProcessableByteArray(content);
        CMSSignedData signeddata = generator.generate(cmsdata, true);
        return signeddata.getEncoded();
    }

    private String generateUniqueID() {
        String randomID = UUID.randomUUID().toString();
        String[] idArray = randomID.split("-");
        return idArray[0];
    }

}
