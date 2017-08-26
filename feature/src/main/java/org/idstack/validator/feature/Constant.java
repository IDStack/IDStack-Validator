package org.idstack.validator.feature;

/**
 * @author Chanaka Lakmal
 * @date 28/5/2017
 * @since 1.0
 */

public class Constant {

    public static class GlobalAttribute {
        public final static String PROPERTIES_FILE_NAME = "idstack.properties";
        public final static String BASIC_CONFIG_FILE_NAME = "basic.config.idstack";
        public final static String DOCUMENT_CONFIG_FILE_NAME = "document.config.idstack";
        public final static String WHITELIST_CONFIG_FILE_NAME = "whitelist.config.idstack";
        public final static String BLACKLIST_CONFIG_FILE_NAME = "blacklist.config.idstack";
        public final static String CONFIG_FILE_PATH = "CONFIG_FILE_PATH";
        public final static String PVT_CERTIFICATE = "PVT_CERTIFICATE";
        public final static String PUB_CERTIFICATE = "PUB_CERTIFICATE";
        public final static String PVT_CERTIFICATE_TYPE = "PVT_CERTIFICATE_TYPE";
        public final static String PUB_CERTIFICATE_TYPE = "PUB_CERTIFICATE_TYPE";
        public final static String PVT_CERTIFICATE_FILE_PATH = "PVT_CERTIFICATE_FILE_PATH";
        public final static String PUB_CERTIFICATE_FILE_PATH = "PUB_CERTIFICATE_FILE_PATH";
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