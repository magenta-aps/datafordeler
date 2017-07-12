# Installation

## Quick start

1. Download source code from github, https://github.com/magenta-aps/datafordeler-sts
1. Unpack to a directory of your choice
1. Download WSO2 Identity Server fro http://wso2.com/identity-and-access-management and place the zip file in the `idp` subfolder of the unpacked source code
1. Rename the downloaded WSO2 zip file to `wso2.zip`
1. Run `initial_setup.bat`
1. Copy `wso2/repository/conf/datasources/master-datasources.xml.example` to `wso2/repository/conf/datasources/master-datasources.xml` and adjust it as neccessary.
1. Copy `wso2/repository/deployment/server/userstores/DATAFORDELER.xml.example` to `wso2/repository/deployment/server/userstores/DATAFORDELER.xml` and adjust as neccessary.
1. Copy `wso2/repository/conf/user-mgt.xml.example` to `wso2/repository/conf/user-mgt.xml` and adjust the admin password.
1. Copy `wso2/repository/conf/carbon.xml.example` to `wso2/repository/conf/carbon.xml` and edit it to configure `HostName`, `MgtHostName` and `KeyStore` settings
1. Copy `wso2/repository/conf/tomcat/catalina-server.xml.example` to `wso2/repository/conf/tomcat/catalina-server.xml` and edit it to adjust KeyStore .jks file location.
1. Run `wso2/bin/wso2server.bat -Dsetup` once to initialize the database
1. Run `run_wso2_server.bat` to start the WSO2 server
