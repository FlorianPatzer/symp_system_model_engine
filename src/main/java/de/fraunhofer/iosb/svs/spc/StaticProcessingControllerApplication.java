package de.fraunhofer.iosb.svs.spc;

import de.fraunhofer.iosb.svs.spc.db.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class StaticProcessingControllerApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StaticProcessingControllerApplication.class);


    private final TargetSystemRepository targetSystemRepository;
    private final TaskRepository taskRepository;
    private final OntologyDependencyRepository ontologyDependencyRepository;
    private final FTPClientService ftpClientService;
    private final ResourceLoader resourceLoader;

    @Value("${mountpoint.ontologies}")
    private String ontologiesMounpoint;

    @Autowired
    public StaticProcessingControllerApplication(TargetSystemRepository targetSystemRepository,
                                                 TaskRepository taskRepository,
                                                 OntologyDependencyRepository ontologyDependencyRepository,
                                                 FTPClientService ftpClientService,
                                                 ResourceLoader resourceLoader) {
        this.targetSystemRepository = targetSystemRepository;
        this.taskRepository = taskRepository;
        this.ontologyDependencyRepository = ontologyDependencyRepository;
        this.ftpClientService = ftpClientService;
        this.resourceLoader = resourceLoader;
    }

    public static void main(String[] args) {
        SpringApplication.run(StaticProcessingControllerApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        // dummy data
        if (targetSystemRepository.findByName("Test System").isEmpty() && targetSystemRepository.findByName("Test System unfinished").isEmpty()) {
            log.debug("Writing Test System");
            Set<OntologyDependency> ontologyDependencies = new HashSet<>();
            OntologyDependency o1 = new OntologyDependency();
            o1.setId("Fraunhofer_IOSB_Security_Systems_OntologyDependency");
            ontologyDependencies.add(o1);
            ontologyDependencyRepository.saveAll(ontologyDependencies);
            Set<Task> taskSet = Stream.of("Pfsense_Effective_Configuration_Implementation",
                    "Merge_IPv4_Networks_Implementation",
                    "Associate_Assets_With_IPv4_Networks_Implementation",
                    "Add_IPv4_Interfaces_to_Parent_Networks_Implementation",
                    "Associate_Allowed_IPv4_Flows_to_Subnets_Implementation").map(id -> {
                Task t1 = new Task();
                t1.setId(id);
                return t1;
            }).collect(Collectors.toSet());
            taskRepository.saveAll(taskSet);

            // finished targetSystem
            TargetSystem targetSystem1 = new TargetSystem();
            targetSystem1.setTasks(taskSet);
            targetSystem1.setOntologyDependencies(ontologyDependencies);
            targetSystem1.setName("Test System");
            String filename = "out_ServiceTask_0xx9o1j.owl";
            String filePath = ontologiesMounpoint + "/" + filename;
            ftpClientService.uploadFile(filename, resourceLoader.getResource("classpath:ontologies/" + filename).getInputStream());
            targetSystem1.setOntologyPath(filePath);
            targetSystemRepository.save(targetSystem1);

            log.debug("Writing Test System unfinished");
            // unfinished targetSystem
            TargetSystem targetSystem2 = new TargetSystem();
            targetSystem2.setTasks(taskSet);
            targetSystem2.setOntologyDependencies(ontologyDependencies);
            targetSystem2.setName("Test System unfinished");
            targetSystemRepository.save(targetSystem2);
        }
        if (targetSystemRepository.findByName("Test System Mensch").isEmpty()) {
            log.debug("Adding: Test System Mensch");

            TargetSystem targetSystem3 = new TargetSystem();
            targetSystem3.setTasks(new HashSet<>());
            targetSystem3.setOntologyDependencies(new HashSet<>());
            targetSystem3.setName("Test System Mensch");
            String filename = "sysont.owl";

            targetSystem3.setOntologyPath(ontologiesMounpoint + "/" + filename);
            ftpClientService.uploadFile(filename, resourceLoader.getResource("classpath:ontologies/" + filename).getInputStream());
            targetSystemRepository.save(targetSystem3);
        } else {
            log.debug("Exists: Test System Mensch");

        }
        if (targetSystemRepository.findByName("X2OWL Collected and merged").isEmpty()) {
            log.debug("Adding: X2OWL Collected and merged");

            TargetSystem targetSystem4 = new TargetSystem();
            Set<Task> taskSet4 = Stream.of("x2owl",
                    "Merge_Redundant_Netwoks_and_Build_Network_Hierarchy",
                    "SWRL_Task_Collection").map(id -> {
                Task t1 = new Task();
                t1.setId(id);
                return t1;
            }).collect(Collectors.toSet());
            taskRepository.saveAll(taskSet4);
            targetSystem4.setTasks(taskSet4);
            targetSystem4.setOntologyDependencies(new HashSet<>());
            targetSystem4.setName("X2OWL Collected and merged");
            String filename = "ServiceTask_0xx9o1j.owl";
            targetSystem4.setOntologyPath(ontologiesMounpoint + "/" + filename);
            ftpClientService.uploadFile(filename, resourceLoader.getResource("classpath:ontologies/" + filename).getInputStream());
            targetSystemRepository.save(targetSystem4);
        } else {
            log.debug("Exists: X2OWL Collected and merged");
        }
    }
}
