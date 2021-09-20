Datafordeler
============

Repository for the datafordeler project.

Also main repository for documentation of the project.
For specific documentation about the documentation, see [This README](docs/README.md)

# Usage:
To clone this repository, please run:

     git clone git@git.magenta.dk:gronlandsprojekter/datafordeler.git

     
On Windows (inside `git-on-windows`), the clone command may have to become:

     ssh-agent bash -c 'ssh-add; git clone git@git.magenta.dk:gronlandsprojekter/datafordeler.git'

# separate projects:
The project consists of different projects which should be developed separately, and which generates its own *.jar file.
Some projects is dependant of the *.jar files in other projects.

There is a project in the folder core, and 11 projects in the folder plugin.

## Plugins:
Each plugin has its own pom.xml. Some plugins depends on other plugins the following projects exists in plugins:

### cpr:
This project is parsing data from cpr, and persists it in the database.
A more detailed description can be found in plugins/cpr/README.md

### cvr:
This project is parsing data from virk.dk, and persists it in the database.
A more detailed description can be found in plugins/cvr/README.md

### geo:
This project is parsing data from GAR, and persists it in the database
A more detailed description can be found in plugins/geo/README.md

### ger:
This project is parsing data from ger, and persists it in the database.
A more detailed description can be found in plugins/ger/README.md

### eboks:
This project exposes data from cpr and cvr.
The project is used for fetching information about the possibility of sending eboks messages for persons 
and companies in datafordeler.
A more detailed description can be found in plugins/eboks/README.md

### prisme:
This project exposes data from from cpr, cvr, ger and geo.
The project is used for fetching information about persons and companies in datafordeler, and exposing it for prisme.
A more detailed description can be found in plugins/prisme/README.md

### combinedPitu:
This project exposes data from from cpr, cvr, ger and geo.
The project is used for fetching information about persons and companies in datafordeler, and exposing it for pitu.
A more detailed description can be found in plugins/combinedPitu/README.md

### subscription:
This project exposes data from from cpr, cvr, ger and geo.
The project is used for fetching information about persons and companies which has changed according to rules defined by subscribers.
A more detailed description can be found in plugins/subscription/README.md

### gladdreg:
This project is parsing data from gladdreg, and persists it in the database.
Data in gladdreg is not maintained any more, but it will not be removed yet either.

### adresseservice:
This project exposes data from gladdreg, it is not used by any customers, and is considered deprecated.

### statistik:
This project exposes data from cpr and geo, it is used for stating batch-jobs for generation of statistical information, and lists of occurrences of events on persons.
A more detailed description can be found in plugins/statistik/README.md

# Compiling/building/testing:

## Tools to use during development

Datafordeler is developed for Java11. In order to start developing, first install Java (OpenJDK 11.#.#) and maven (3.6.# or higher)
The easiest way to develop for the project is by using IntelliJ or eclipse, but it can be done with any text-editor.
The best way of developing for the project is by using Linux, the way to work with the project is mainly described with the expectation that the developer uses Linux.

#### Setup of development tools on Linux

##### OpenJDK 11
Installing openJDK can be done by following the information on the page in the following url.
https://openjdk.java.net/install/

If there is installed more than one version of Java, the default version to be used during build can be changed with the following command.

update-alternatives --config java

##### IntelliJ
Installing openJDK can be done by following the information on the page in the following url:
https://www.jetbrains.com/help/idea/installation-guide.html#standalone

##### Maven
Installing maven can be done by following the information on the page in the following url:
https://maven.apache.org/install.html

## Building and testing the application
For building and testing the full application, all plugins need to be compiled into a *.jar file.
Some plugins are dependent on other plugins; it is necessary to compile the different plugins in a specific sequence to get the application running on the development machine.

### Maven
Building and testing the application can be done through maven.
For small changes to the project it is sufficient. 

1. Go to datafordeler/core and run "mvn -DskipTests clean install"
2. Go to datafordeler/plugin/cpr and run "mvn -DskipTests clean install"
3. Go to datafordeler/plugin/cvr and run "mvn -DskipTests clean install"
4. Go to datafordeler/plugin/geo and run "mvn -DskipTests clean install"
5. Go to datafordeler/plugin/ger and run "mvn -DskipTests clean install"
6. Go to datafordeler/plugin/subscription and run "mvn -DskipTests clean install"
7. Go to datafordeler/plugin/eboks and run "mvn -DskipTests clean install"
8. Go to datafordeler/plugin/prisme and run "mvn -DskipTests clean install"
9. Go to datafordeler/plugin/combinedPitu and run "mvn -DskipTests clean install"
10. Go to datafordeler/plugin/statistik and run "mvn -DskipTests clean install"

After that go to the specific plugin where a code change is needed and run:
"mvn test"
In order of validating that the code-change is correct, and that it did not break any existing tests

### IntelliJ
If major changes are supposed to be done, the developer need a development tool like IntelliJ
Each plugin needs to be opened seperately. 
If for instance the developer needs to make changes to core, start IntelliJ, click File-open, and select to open datafordeler/core
IntelliJ identifies the maven file itself pom.xml, and loads the project and dependencies.
Find the maven-view.
compile to compile the plugin, test to test the project.
Some modules depend on other modules, in order to get started install all plugins by clicking install on each plugin.
The sequence of installing the plugins is:

1. datafordeler/core
2. datafordeler/plugin/cpr
3. datafordeler/plugin/cvr
4. datafordeler/plugin/geo
5. datafordeler/plugin/ger
6. datafordeler/plugin/subscription
7. datafordeler/plugin/eboks
8. datafordeler/plugin/prisme
9. datafordeler/plugin/combinedPitu
10.datafordeler/plugin/statistik

### Docker
If a developer wants to build the project in docker, it is possible to run:
"docker-compose build" and "docker-compose up -d". It is not recommended to work with the project in that way though.

## Building the online API-documentation
The online API-documentation is done inside vagrant. To build the online documentation we use the vagrant image from "gronlandsprojekter"
The vagrant project is cloned from the the following link:
git@git.magenta.dk:gronlandsprojekter/vagrant.git
into the folder datafordeler/vagrant
From the folder vagrant:
2. vagrant up
3. vagrant ssh
4. sudo apt install python-pip
5. pip install javasphinx
6. pip install -U sphinx
7. cd /vagrant/docs/
8. sudo apt-get install python3-venv
9. python3 -m venv env
10. source ./env/bin/activate
11. pip install --upgrade pip
12. pip install git+https://github.com/bronto/javasphinx.git
13. exit
14. cd /vagrant/
15. ./gen_doc.sh

The documentation is placed in the path C:\inetpub\dafodoc\openapi on the servers dafomgmt01 and dafomgmttest01


### Testdata and regressiontests
All fetching of data in production server, is done by feching files from the sources, typically the servers for GAR, CPR, and CVR.
In production fetching of data is started by calling "/command/pull" or adding a row to the table "command"

In this application we are using files which corresponds to what we receive as live data in production.

In regressiontests we load data into tests with testfiles stored in separate modules in the resources folders.
The different testfiles is created for different types of persons, which match the test-scenarions.



## Installation of application on Windows server

When deploying Datafordeler on the test- and production- servers, we run the file run_server_notests.bat. This file compiles and builds the full project.
After running the *.bat file the project, including all plugins is compiles, and stored in the file-system, and the server is started.
The *.bat file is created for the deployment om production-servers, but is can be used during development on windows machines as well.




