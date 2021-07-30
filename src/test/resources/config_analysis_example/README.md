# What is it about?
The example contains phase workflows which create a system model from 9 files containing command outputs of respective 9 devices (they can be found in the collect phase's folder).
The workflows further extend the system model to be able to perform network access control configuration analysis following the dissertation "Automatisierte, minimalinvasive Sicherheitsanalyse und Vorfallreaktion für industrielle Systeme".

# Requirements
The example requires the following software:

|	Software	|	Version/tag	|	Repository	|
|-----------------------|-----------------------|-----------------------|
|	SyMP Docker	|	Tag: 1.0.0	|[SyMP Docker](git@gitlab-ext.iosb.fraunhofer.de:symp/symp-docker.git)|
|Camunda Modeler	|	Tag: 1.0.1	|[camunda-modeler-plugin](git@gitlab-ext.iosb.fraunhofer.de:symp/camunda-modeler-plugin.git)|
|file2owl Worker	|	Tag: config_analysis_diss |	[file2owl](git@gitlab-ext.iosb.fraunhofer.de:symp/external-workers/file2owl.git)|
|Effective Configuration Worker|Tag: 1.0.1	|[fw_effective_configuration](git@gitlab-ext.iosb.fraunhofer.de:symp/external-workers/fw_effective_configuration.git)|


The main folders of x2owl and camunda worker libraries have to be added to the PYTHONPATH variable (or to interpreter phaths, if e.g. pycharm is used to run the file2owl worker).
E.g.:
```
export PYTHONPATH=$PYTHONPATH:/var/lib/x2owl
```
# Run
The SyMP Docker project does not need any special preperation. The containers can be started via ```docker-compose up``` and the FTP server has to contain every Model referenced by ftp://ftp... within the workflows.
## Collect
For the collection phase, the file2owl worker has to be configured to use the correct camunda endpoint (by replacing the docker-container-ip):
```
if __name__ == '__main__':
    print("Worker started")

    url = 'http://docker-container-ip:8080/engine-rest'
```

Next the worker has to be started, by starting the run.py.
Afterwards the workflow can be started (e.g. using the Camunda Modeler).
When the worker finished, the worker's output directory contains the resulting component models.
If the collect phase shall be skipped, the output directory of the collect directory contains component models which can be used for later phases.

## Fuse and Clean
The phase does not need any external workers but contains a workflow.

## Static Knowledge Extension
The phase does only need the Effective Configuration Worker, which should be started via the ```run_docker.sh``` script within its project directory.

## Dynamic Knowledge Extension
The phase does not need any external workers but contains a workflow.

## Analysis
The phase does not need any external workers but contains a workflow.

## Query
The example's SPARQL query (located in the example's main folder) can be performed on the resulting model from the analysis phase. For that, Protegé or GraphDB could be used.


