# IDStack-Validator

Validator API for document validation with the use of digital signatures

---
- spring_boot_version: 1.5.2.RELEASE
- java_version: 1.8
- tomcat_version: 8.0.43
- maven_version: 3.5
---

## Set up the project

- Make a clone of this project
- Update `api/src/main/java/resources/idstack.properties` file

```
API_KEY=8742b79d-08d7-4e1b-a4ce-27b4817348a1
CONFIG_FILE_PATH=/usr/local/idstack/validator/
PVT_CERTIFICATE_FILE_PATH=/usr/local/idstack/validator/cert/pvt/
PVT_CERTIFICATE_TYPE=.pfx
PVT_CERTIFICATE_PASSWORD_TYPE=.pw
PUB_CERTIFICATE_FILE_PATH=/usr/local/idstack/validator/cert/pub/
PUB_CERTIFICATE_TYPE=.cer
STORE_FILE_PATH=/usr/local/idstack/validator/docs/
```

- Build the project in order to create `.war` file
```
$ mvn clean install
```

- Deploy the `.war` file on Tomcat server : https://tomcat.apache.org/tomcat-7.0-doc/deployer-howto.html

- Access the webservice : http://localhost:8080/api-validator/

## Summary

Congratulations! You've created the web service of the Validator

API-Documentation: http://docs.idstack.apiary.io/