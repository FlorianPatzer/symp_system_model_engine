package de.fraunhofer.iosb.svs.spc;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.springframework.stereotype.Service;

import de.fraunhofer.iosb.svs.spc.db.OntologyDependency;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

@Service
public class ModelProcessingService {
    
    private static final Logger log = LoggerFactory.getLogger(ModelProcessingService.class);

    private InputStream loadFromUrl(String fileUrl) throws IOException  {
        URL url = new URL(fileUrl);
        URLConnection conn = url.openConnection();
        InputStream fileInputStream = conn.getInputStream();
        
        return fileInputStream;
    }

    public List<String> extractServiceTaskNames(InputStream fileInputStream) {
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(fileInputStream);
        Collection<ServiceTask> serviceTasks = modelInstance.getModelElementsByType(ServiceTask.class);
        List<String> serviceNamesList = new ArrayList<String>();

        for (ServiceTask serviceTask : serviceTasks) {
            serviceNamesList.add(serviceTask.getName());
        }

        return serviceNamesList;
    }
    
    public List<String> extractServiceTaskNames(String fileUrl) throws IOException {
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(loadFromUrl(fileUrl));
        Collection<ServiceTask> serviceTasks = modelInstance.getModelElementsByType(ServiceTask.class);
        List<String> serviceNamesList = new ArrayList<String>();

        for (ServiceTask serviceTask : serviceTasks) {
            serviceNamesList.add(serviceTask.getName());
        }

        return serviceNamesList;
    }


    public String extractLastTaksId(InputStream fileInputStream) {
        String taskId = null;

        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(fileInputStream);
        Collection<EndEvent> endEvents = modelInstance.getModelElementsByType(EndEvent.class);
        Collection<ServiceTask> serviceTasks = modelInstance.getModelElementsByType(ServiceTask.class);

        EndEvent endEvent = endEvents.iterator().next();
        SequenceFlow endEventIncomingSeqFlow = endEvent.getIncoming().iterator().next();

        for (ServiceTask serviceTask : serviceTasks) {
            for (SequenceFlow taskOutgoingSeqFlow : serviceTask.getOutgoing()) {
                if (taskOutgoingSeqFlow == endEventIncomingSeqFlow) {
                    taskId = serviceTask.getId();
                }
            }
        }

        return taskId;
    }
    
    public String extractLastTaksId(String fileUrl) throws IOException {
        String taskId = null;

        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(loadFromUrl(fileUrl));
        Collection<EndEvent> endEvents = modelInstance.getModelElementsByType(EndEvent.class);
        Collection<ServiceTask> serviceTasks = modelInstance.getModelElementsByType(ServiceTask.class);

        EndEvent endEvent = endEvents.iterator().next();
        SequenceFlow endEventIncomingSeqFlow = endEvent.getIncoming().iterator().next();

        for (ServiceTask serviceTask : serviceTasks) {
            for (SequenceFlow taskOutgoingSeqFlow : serviceTask.getOutgoing()) {
                if (taskOutgoingSeqFlow == endEventIncomingSeqFlow) {
                    taskId = serviceTask.getId();
                }
            }
        }

        return taskId;
    }

    public Set<OntologyDependency> extractOntologyDependencies(InputStream fileInputStream)
            throws JDOMException, IOException {

        Set<OntologyDependency> ontDependencies = new HashSet<OntologyDependency>();

        // Load file
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(fileInputStream);
        Element classElement = document.getRootElement();

        // Load child nodes
        List<Element> children = classElement.getChildren();

        // Search for the nodes containing an ontology dependency
        List<Element> searchedElement = new ArrayList<Element>();
        for (Element child : children) {
            String name = child.getName();
            if (name == "Ontology" || name == "versionIRI") {
                searchedElement.add(child);
                break;
            }
        }

        // Extract the dependencies
        for (Element element : searchedElement) {
            List<Attribute> attributes = element.getAttributes();
            for (Attribute att : attributes) {
                String attName = att.getName();
                if (attName == "about" || attName == "resource") {
                    OntologyDependency ont = new OntologyDependency();
                    ont.setId(att.getValue());
                    ontDependencies.add(ont);
                }
            }
        }

        return ontDependencies;
    }

    public Set<OntologyDependency> extractOntologyDependencies(String fileUrl)
            throws JDOMException, IOException {

        Set<OntologyDependency> ontDependencies = new HashSet<OntologyDependency>();

        // Load file
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(loadFromUrl(fileUrl));
        Element classElement = document.getRootElement();

        // Load child nodes
        List<Element> children = classElement.getChildren();

        // Search for the nodes containing an ontology dependency
        List<Element> searchedElement = new ArrayList<Element>();
        for (Element child : children) {
            String name = child.getName();
            if (name == "Ontology" || name == "versionIRI") {
                searchedElement.add(child);
                break;
            }
        }

        // Extract the dependencies
        for (Element element : searchedElement) {
            List<Attribute> attributes = element.getAttributes();
            for (Attribute att : attributes) {
                String attName = att.getName();
                if (attName == "about" || attName == "resource") {
                    OntologyDependency ont = new OntologyDependency();
                    ont.setId(att.getValue());                    
                    ontDependencies.add(ont);
                }
            }
        }

        return ontDependencies;
    }

}
