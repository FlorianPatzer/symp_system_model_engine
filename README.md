## About The Project
System Model Engine

### Built With

The SME is built as a Docker orchestration with the following frameworks:

* [Spring](https://spring.io/)
* [Angular](https://angular.io/)

## Getting Started
This project is dependent on the following projects:

* [SyMP Base](https://gitlab-ext.iosb.fraunhofer.de/symp/symp-docker) 

### Prerequisites

This project uses Docker to automatically build and setup the created service. Please make sure that you have Docker isntalled or follow the download instructions [here](https://docs.docker.com/docker-for-windows/install/).

The System Model Engine needs a connection to a MySQL Database and an FTP Server.
The Following vaiables can be adapted to change the used entpoints for these services.
The property files are located in  the`src/main/resources` folder.

### Installation
**Option 1: Using Docker**

*Attention:* The dockerized build is already preconfigured and uses the `prod` profile. 

   - One-Liner: Use `docker-compose up --build` to build the containers and start them directly
   - Use `docker-compose build` to build the containers.
   - Use `docker-compose up` to start the containers.

**Option 2: Running in DEV mode locally**

To start the Engine outside Docker do the following:

- Run SME with maven:
    1. `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

- Run the frontend:
    1. Navigate to `sme-gui` folder
    2. Run `npm install`
    3. Run `ng serve`
    4. The fronte end should be available at http://localhost:4200/#/home

*Note: The env profiles are already configured and the service should work out of the box*

## Usage

* The System Model Engine follows the OpenAPI specification. A Swagger documentation is available at `docs/reference/`.

* The SME