package org.idstack.validator;

import com.google.gson.Gson;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.Base64;
import org.idstack.feature.Parser;
import org.idstack.feature.document.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Chandu Herath
 * @date 08/06/2017
 * @since 1.0
 */

public class SignatureVerifier {

    public static final String PUBLICCCER = "BuddhikaWijebandara.cer";
    private Parser jsonParser = new Parser();
    private VerificationPreProcessor verificationPreProcessor = new VerificationPreProcessor();

    public ArrayList<Boolean> verifyJson(String jsonString) throws CMSException, OperatorCreationException, FileNotFoundException, CertificateException {
        Document originalDigitalJsonSigned = jsonParser.parseDocumentJson(jsonString);
        ArrayList<Boolean> verifiedArray = new ArrayList<>();

        for (int i = 0; i < originalDigitalJsonSigned.getValidators().size(); i++) {
            Document clonedDigitalJsonSigned = jsonParser.parseDocumentJson(jsonString);
            Document digitalJsonSigned = verificationPreProcessor.getSignedDigitizedVersion(clonedDigitalJsonSigned, i);
            verifiedArray.add(verify(originalDigitalJsonSigned, digitalJsonSigned, i));
        }
        return verifiedArray;
    }


    private boolean verify(Document originalDigitalJsonSigned, Document digitalJsonSigned, int signatureIndex) throws OperatorCreationException, FileNotFoundException, CertificateException {
        String toVerify = new Gson().toJson(digitalJsonSigned);

        String signed = originalDigitalJsonSigned.getValidators().get(signatureIndex).getSignature().getMessageDigest();
        byte[] signedByte = Base64.decode(signed);
        boolean result = false;
        try {
            CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(toVerify.getBytes()), signedByte);

            SignerInformationStore signers = s.getSignerInfos();
            SignerInformation signerInfo = (SignerInformation) signers.getSigners().iterator().next();
            result = signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(this.getPublicCertificate().getPublicKey()));
            getOwnerDetails();
        } catch (CMSException e) {
            e.printStackTrace();
        }

        return result;

    }

    private Certificate getPublicCertificate() throws FileNotFoundException, CertificateException {
        FileInputStream fis = new FileInputStream(getClass().getClassLoader().getResource(PUBLICCCER).getFile());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificates(fis).iterator().next();
        return cert;
    }

    private HashMap<String, String> getOwnerDetails() throws FileNotFoundException, CertificateException {
        HashMap<String, String> ownerDetails = new HashMap<>();
        Certificate publicCertificate = getPublicCertificate();
        String row = ((X509Certificate) publicCertificate).getIssuerX500Principal().toString();
        String[] fields = row.split(",");
        for (int i = 0; i < fields.length; i++) {
            String[] keyValue = fields[i].split("=");
            ownerDetails.put(keyValue[0], keyValue[1]);
        }
        return ownerDetails;
    }
}
