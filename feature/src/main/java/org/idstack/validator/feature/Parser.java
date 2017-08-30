package org.idstack.validator.feature;

import com.google.gson.*;
import org.idstack.validator.feature.document.Document;
import org.idstack.validator.feature.document.Extractor;
import org.idstack.validator.feature.document.MetaData;
import org.idstack.validator.feature.document.Validator;

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
     * validators = an array of json objects
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
        JsonArray validatorsObject = obj.getAsJsonArray(Constant.JsonAttribute.VALIDATORS);

        //create LinkedHAshMap
        LinkedHashMap<String, Object> contentMap = parseContent(contentObject);

        //create metadata object
        MetaData metaData = new Gson().fromJson(metadataObject.toString(), MetaData.class);

        //create extractor object
        Extractor extractor = new Gson().fromJson(extractorObject.toString(), Extractor.class);

        //create validator list
        ArrayList<Validator> validators = parseValidators(validatorsObject);

        Document doc = new Document(metaData, contentMap, extractor, validators);

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
     * validators = an array of json objects
     * |-- id = string
     * |-- signature = json object
     * |-- signedAttributes = String Array
     * |-- signedSignatures = String Array
     *
     * @param validatorJsonArray json array of validators
     * @return list of validator objects
     */
    private static ArrayList<Validator> parseValidators(JsonArray validatorJsonArray) {
        ArrayList<Validator> validators = new ArrayList<>();
        for (JsonElement element : validatorJsonArray) {
            validators.add(new Gson().fromJson(element.getAsJsonObject().toString(), Validator.class));
        }
        return validators;
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
