# IDStack-RelyingParty

Relying Party API for document analyzing with the help of confidence score and correlation score

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
API_KEY=e489e5ce-e80a-4334-8864-abfa86e6014c
CONFIG_FILE_PATH=/usr/local/idstack/relyingparty/
STORE_FILE_PATH=/usr/local/idstack/relyingparty/docs/
```

- Build the project in order to create `.war` file
```
$ mvn clean install
```

- Deploy the `.war` file on Tomcat server : https://tomcat.apache.org/tomcat-7.0-doc/deployer-howto.html

- Access the webservice : http://localhost:8080/api-relyingparty/

## Summary

Congratulations! You've created the web service of the RelyingParty

API-Documentation: http://docs.idstack.apiary.io/