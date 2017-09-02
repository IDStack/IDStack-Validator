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
import org.idstack.feature.document.Signature;
import org.idstack.feature.document.Validator;

import java.io.File;
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
 * @date 26/05/2017
 * @since 1.0
 */

public class JsonSigner {

    public static final String SIGNATUREALGO = "SHA256withRSA";
    public static String PKCS12FILE;
    public static char[] PKCS12PASSWORD;
    public static String PUBLICCERURL;

    private Parser jsonParser = new Parser();
    private SignPreProcessor signPreProcessor = new SignPreProcessor();

    public JsonSigner(String privateCertFilePath, String password, String publicCertURL){
        this.PKCS12FILE = privateCertFilePath;
        this.PKCS12PASSWORD = password.toCharArray();
        this.PUBLICCERURL = publicCertURL;
    }

    public String signJson(String jsonString, boolean signContent, ArrayList<String> signList) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, OperatorCreationException, CMSException, IOException, CloneNotSupportedException, NoSuchProviderException {

        Document digitizedDocument = jsonParser.parseDocumentJson(jsonString);
        Document signOnlyDigitizedDocument = jsonParser.parseDocumentJson(jsonString);

        signOnlyDigitizedDocument = signPreProcessor.getSignOnlyDigitizedVersion(signOnlyDigitizedDocument, signContent, signList);
        digitizedDocument = sign(signOnlyDigitizedDocument, digitizedDocument, signContent, signList);

        return new Gson().toJson(digitizedDocument);
    }

    private Document sign(Document signOnlyDigitalJson, Document completeDigitalJson, boolean signContent, ArrayList<String> signList) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, CMSException,
            OperatorCreationException, NoSuchProviderException {

        String jsonStringToSign = new Gson().toJson(signOnlyDigitalJson);

        //Setting BouncyCastle as the security provoder
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        //Load the .pfx file into a key store
        KeyStore keyStore = this.loadKeyStore(provider);

        CMSSignedDataGenerator signatureGenerator = this.setUpProvider(keyStore);
        byte[] signedBytes = this.signMessagePKCS7(jsonStringToSign.getBytes("UTF-8"), signatureGenerator);
        String signedString = new String(Base64.encode(signedBytes));

        String signerID = generateUniqueID();//generate signature ID
        Signature signature = new Signature(signedString, PUBLICCERURL);
        if (signList == null) {
            signList = new ArrayList<>();
        }
        Validator newSigner = new Validator(signerID, signature, signContent, signList);

        completeDigitalJson.getValidators().add(newSigner);

        return completeDigitalJson;
    }

    private KeyStore loadKeyStore(BouncyCastleProvider provider) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyStore ks = KeyStore.getInstance("PKCS12", provider.getName());
        ks.load(new FileInputStream(new File(PKCS12FILE)), PKCS12PASSWORD);
        return ks;
    }

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
