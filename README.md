# CardCreate

'CardCreate' is a Java application that generates images representing playing 
cards.

## Overview

'CardCreate' generates .png images representing a pack of playing cards from 
existing images. It is very configurable, allowing for playing cards of 
greatly varying styles to be created.

To use 'CardCreate' you will need a Java Development Kit and Maven installed. 

## Cloning and Building

The code has been structured as a standard Maven project which means you need 
to have Maven and a JDK installed. A quick web search will help, but if not 
https://maven.apache.org/install.html should guide you through the install.

The following commands clone and generate an executable jar file in the 
"target" directory:

    git clone https://github.com/PhilLockett/CardCreate.git
	cd CardCreate/
    mvn clean install

## Setting up the 'CardCreate' environment

'CardCreate' works in an environment which provides the component images 
needed to compose the playing card images. BEFORE running 'CardCreate' it is 
recommended that the environment is setup first. The GitHub repository 
contains the file 'CardCreate/CardWork.tar.gz' which provides this 
environment. It is recommended that this environment is set up outside of the 
'CardCreate' directory. The environment can be set up in the parent 
directory of 'CardCreate' with the following commands:

    cd ..
    cp CardCreate/CardWork.tar.gz .
    tar zxf CardWork.tar.gz
    cd CardWork/
    ./setup.sh

## Running

'CardCreate' can be launched using a file explorer or from the command line:

    java -jar ./target/CardCreate-jar-with-dependencies.jar

On initial running, 'CardCreate' requires you to select the environment you 
setup. Browse to the CardWork directory created above from the 
CardCreate/CardWork.tar.gz file. Once setup you will not be prompted again, 
however, you can click on the "Browse..." button at any time to select another 
instance of the environment.

The standard `mvn clean` command will remove all generated files, including 
any environment file paths previously set up.

## Further reading

The document 'Card Generator User Guide.pdf' describes the installation, the 
environment set up and 'CardCreate' usage with many examples.

## Additional packages

Additional packages are currently unavailable.

## Points of interest

This code has the following points of interest:

  * CardCreate is the Java version of cardgen.
  * cardgen: https://github.com/PhilLockett/cardgen.git
  * CardCreate is a maven project.
  * The user GUI was developed using NetBeans.
  * The NetBeans .form files are supplied to ease GUI design changes.
  