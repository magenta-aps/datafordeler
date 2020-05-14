Datafordeler
============

Repository for the datafordeler project.

Also main repository for documentation of the project.
For specific documentation about the documentation, see [This README](docs/README.md)

## Usage:
To clone this repository, please run:

     git clone git@git.magenta.dk:gronlandsprojekter/datafordeler.git

     
On Windows (inside `git-on-windows`), the clone command may have to become:

     ssh-agent bash -c 'ssh-add; git clone git@git.magenta.dk:gronlandsprojekter/datafordeler.git'

## seperate projects:
The project consists of different projects which should be developed separately, and which generates its own *.jar file.
Some projects is dependant of the *.jar files in other projects.

There is a project in the folder core, and 9 projects in the folder plugin.

## Plugins:
Each plugin has its own pom.xml. Some plugins depends on other plugins the following projects exists in plugins:

cpr:
This project is parsing data from cpr, and persists it in the database.
A more detailed description can be found in plugins/cpr/README.md

cvr:
This project is parsing data from virk.dk, and persists it in the database.
A more detailed description can be found in plugins/cpr/README.md

geo:
This project is parsing data from GAR, and persists it in the database
A more detailed description can be found in plugins/cpr/README.md

ger:
This project is parsing data from ger, and persists it in the database.
A more detailed description can be found in plugins/cpr/README.md

eboks:
This project exposes data from from cpr and cvr.
The project is used for fetching information about the possibility of sending eboks messages for persons 
and companies in datafordeler.
A more detailed description can be found in plugins/cpr/README.md

prisme:
This project exposes data from from cpr, cvr, ger and geo.
The project is used for fetching information about persons and companies in datafordeler.
This plugin deliveres inform
A more detailed description can be found in plugins/cpr/README.md

gladdreg:
This project is parsing data from gladdreg, and persists it in the database.
Data in gladdreg is not maintained any more, but it will not be removed yet either.

adresseservice:
This project exposes data from gladdreg, it is not used by any customers, and is considered deprecated.
A more detailed description can be found in plugins/cpr/README.md

statistik:
This project exposes data from cpr and geo, it is not used by any customers, and is considered deprecated
A more detailed description can be found in plugins/cpr/README.md

## Compiling/building/testing:

Datafordeler is developed for Java11, in order to start developing first install Java and maven

Go to core, or one of the plugins and execute 'mvn clean install'
The command will build the plugin, start the servers, and run the tests




