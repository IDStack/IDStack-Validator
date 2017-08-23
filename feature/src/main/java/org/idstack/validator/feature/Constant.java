package org.idstack.validator.feature;

/**
 * @author Chanaka Lakmal
 * @date 28/5/2017
 * @since 1.0
 */

public class Constant {

    public static class GlobalAttribute {
        public static String PROPERTIES_FILE_NAME = "idstack.properties";
        public static String CONFIG_FILE_NAME = "config.idstack";
        public static String CONFIG_FILE_PATH = "CONFIG_FILE_PATH";
    }

    public static class JsonAttribute {

        public static String META_DATA = "meta_data";
        public static String CONTENT = "content";
        public static String EXTRACTOR = "extractor";
        public static String SIGNERS = "signers";

        public static class MetaData {
            public static String NAME = "name";
            public static String VERSION = "version";
            public static String DOCUMENT_ID = "document_id";
            public static String DOCUMENT_TYPE = "document_type";
            public static String ISSUER = "issuer";
        }

        public static class Extractor {
            public static String ID = "id";
            public static String SIGNATURE = "signature";
        }

        public static class Signers {
            public static String ID = "id";
            public static String SIGNATURE = "signature";
            public static String SIGNED_CONTENT = "signed_content";
            public static String SIGNED_SIGNATURES = "signed_signatures";
        }

        public static class Signature {
            public static String MESSAGE_DIGEST = "message_digest";
            public static String URL = "url";
        }
    }
}