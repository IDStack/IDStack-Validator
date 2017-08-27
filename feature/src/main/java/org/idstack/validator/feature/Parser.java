package org.idstack.validator.feature;

import com.google.gson.*;
import org.idstack.validator.feature.document.*;

import java.util.*;

/**
 * @author Sachithra Dangalla
 * @date 5/31/2017
 * @since 1.0
 */
public class Parser {

    /**
     * Parses a documentJson to its corresponding Map
     * Assumes the following structure:
     * document = complex json object with NO ARRAYS
     * verifier = simple json object
     * |-- signature = json object
     * signers = an array of json objects
     * |-- id = string
     * |-- signature = json object
     * |-- signedAttributes = String Array
     * |-- signedSignatures
     *
     * @param jsonString json string
     * @return the corresponding Map
     */
    public static Document parseDocumentJson(String jsonString) {

        JsonObject obj = new JsonParser().parse(jsonString).getAsJsonObject();
        JsonObject metadataObject = obj.getAsJsonObject(Constant.JsonAttribute.META_DATA);
        JsonObject contentObject = obj.getAsJsonObject(Constant.JsonAttribute.CONTENT);
        JsonObject extractorObject = obj.getAsJsonObject(Constant.JsonAttribute.EXTRACTOR);
        JsonArray signersObject = obj.getAsJsonArray(Constant.JsonAttribute.SIGNERS);

        //create LinkedHAshMap
        LinkedHashMap<String, Object> contentMap = parseContent(contentObject);

        //create metadata object
        MetaData metaData = new MetaData(metadataObject.get(Constant.JsonAttribute.MetaData.NAME).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.VERSION).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.DOCUMENT_ID).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.DOCUMENT_TYPE).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.ISSUER).getAsString(), metadataObject.get(Constant.JsonAttribute.MetaData.URL).getAsString());

        //create extractor object
        Extractor extractor = new Extractor(extractorObject.get(Constant.JsonAttribute.Extractor.ID).getAsString(), new Gson().fromJson(extractorObject.get(Constant.JsonAttribute.Extractor.SIGNATURE).toString(), Signature.class));

        //create signer list
        ArrayList<Signer> signers = parseSigners(signersObject);

        Document doc = new Document(metaData, contentMap, extractor, signers);

        return doc;
    }

    /**
     * Creates a Java LinkedHashMap corresponding to a given json object
     * Iterates only the objects - not arrays or primitives
     *
     * @param obj json object of content
     * @return the linked hash map of the content attributes
     */
    private static LinkedHashMap<String, Object> parseContent(JsonObject obj) {

        Set<Map.Entry<String, JsonElement>> set = obj.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive()) {
                map.put(key, parseContent(value.getAsJsonObject()));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }


    /**
     * signers = an array of json objects
     * |-- id = string
     * |-- signature = json object
     * |-- signedAttributes = String Array
     * |-- signedSignatures = String Array
     *
     * @param signerJsonArray json array of signers
     * @return list of signer objects
     */
    private static ArrayList<Signer> parseSigners(JsonArray signerJsonArray) {
        ArrayList<Signer> signers = new ArrayList<>();
        for (JsonElement element : signerJsonArray) {
            JsonObject arrayItem = element.getAsJsonObject();

            //add id
            String id = arrayItem.get(Constant.JsonAttribute.Signers.ID).getAsString();

            //add signature
            JsonObject signatureObject = arrayItem.getAsJsonObject(Constant.JsonAttribute.Signers.SIGNATURE);
            Signature signature = new Gson().fromJson(signatureObject.toString(), Signature.class);

            //add signed content
            boolean signedContent = arrayItem.get(Constant.JsonAttribute.Signers.SIGNED_CONTENT).getAsBoolean();

            //add signed signatures
            ArrayList<String> signedSignatures = new ArrayList<>();
            JsonArray signedSignaturesArray = arrayItem.getAsJsonArray(Constant.JsonAttribute.Signers.SIGNED_SIGNATURES);
            if (signedSignaturesArray != null)
                for (JsonElement sign : signedSignaturesArray) {
                    signedSignatures.add(sign.getAsString());
                }
            //add the entire object to the final arraylist
            signers.add(new Signer(id, signature, signedContent, signedSignatures));
        }
        return signers;
    }


    /**
     * Not used - for future reference only
     *
     * @param objString
     * @param result
     * @return
     */
    private static LinkedHashMap<String, Object> dynamicIterateJsonObject(String objString, LinkedHashMap<String, Object> result) {

        JsonElement element = new JsonParser().parse(objString);
        if (element.isJsonPrimitive()) {
            result.put(element.getAsString(), element.getAsString());
        } else {
            JsonObject obj = element.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entrySet = obj.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getValue().isJsonObject()) {

                    LinkedHashMap<String, Object> objVal = dynamicIterateJsonObject(entry.getValue().toString(), result);
                    result.put(entry.getKey(), objVal);

                } else if (entry.getValue().isJsonArray()) {
                    JsonArray array = entry.getValue().getAsJsonArray();
                    ArrayList<LinkedHashMap<String, Object>> items = new ArrayList<>();

                    for (JsonElement arrayItem : array) {
                        LinkedHashMap<String, Object> arrVal = dynamicIterateJsonObject(arrayItem.toString(), new LinkedHashMap<>());
                        items.add(arrVal);
                    }
                    result.put(entry.getKey(), items);

                } else {
                    result.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return result;
    }
}
