package de.fraunhofer.iosb.svs.spc;

import de.fraunhofer.iosb.svs.spc.db.OntologyDependency;
import de.fraunhofer.iosb.svs.spc.db.OntologyDependencyRepository;
import de.fraunhofer.iosb.svs.spc.db.TargetSystem;
import de.fraunhofer.iosb.svs.spc.db.TargetSystemRepository;
import de.fraunhofer.iosb.svs.spc.db.Task;
import de.fraunhofer.iosb.svs.spc.db.TaskRepository;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class TargetSystemController {
    private static final Logger log = LoggerFactory.getLogger(TargetSystemController.class);

    @Value("${mountpoint.ontologies}")
    private String ontologiesMounpoint;

    @Value("${ftp.address}")
    private String ftpAddress;

    private final TargetSystemRepository targetSystemRepository;
    private final FTPClientService ftpClientService;
    private final ModelProcessingService modelProcessingService;
    private final TaskRepository taskRepository;
    private final OntologyDependencyRepository ontologyDependencyRepository;

    @Autowired
    public TargetSystemController(TargetSystemRepository targetSystemRepository, TaskRepository taskRepository,
            FTPClientService ftpClientService, ModelProcessingService modelProcessingService,
            OntologyDependencyRepository ontologyDependencyRepository) {
        this.targetSystemRepository = targetSystemRepository;
        this.ftpClientService = ftpClientService;
        this.modelProcessingService = modelProcessingService;
        this.taskRepository = taskRepository;
        this.ontologyDependencyRepository = ontologyDependencyRepository;
    }
    
    @GetMapping(path = "/", produces = "application/json")
    public String defaultPath() {
        return "SPC Reached";
    }

    @GetMapping(path = "/targetsystem", produces = "application/json")
    public ResponseEntity<List<TargetSystem>> getTargetSystems() {
        return ResponseEntity.ok(targetSystemRepository.findAll());
    }

    @GetMapping(path = "/targetsystem/{id}", produces = "application/json")
    public ResponseEntity<TargetSystem> getTargetSystems(@PathVariable("id") Long id) {
        return ResponseEntity.ok(targetSystemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("targetSystem", id)));
    }

    @DeleteMapping(path = "/targetsystem/{id}", produces = "application/json")
    public ResponseEntity<Map<String, String>> deleteTargetSystems(@PathVariable("id") Long id) {
        Map<String, String> response = new HashMap<String, String>();
        HttpStatus statusCode;

        Optional<TargetSystem> tsQuery = targetSystemRepository.findById(id);

        if (tsQuery.isPresent()) {
            targetSystemRepository.delete(tsQuery.get());
            response.put("status", "Target system deleted");
            statusCode = HttpStatus.OK;
        } else {
            response.put("status", "No target system with ID " + id.toString());
            statusCode = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<Map<String, String>>(response, statusCode);
    }

    @PutMapping(path = "/targetsystem", produces = "application/json")
    public ResponseEntity<Map<String, String>> updateTargetSystems(@RequestBody TargetSystem ts) {
        Map<String, String> response = new HashMap<String, String>();
        HttpStatus statusCode;

        Optional<TargetSystem> tsQuery = targetSystemRepository.findById(ts.getId());

        if (tsQuery.isPresent()) {
            targetSystemRepository.save(ts);
            response.put("status", "Target system updated");
            statusCode = HttpStatus.OK;
        } else {
            response.put("status", "No target system with ID " + ts.getId().toString());
            statusCode = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<Map<String, String>>(response, statusCode);
    }

    @PostMapping(path = "/targetsystem", produces = "application/json")
    public ResponseEntity<Map<String, String>> createTargetSystems(@RequestBody TargetSystemGenReq ts)
            throws IOException, JDOMException {
        Map<String, String> response = new HashMap<String, String>();
        HttpStatus statusCode;

        TargetSystem example = new TargetSystem();
        example.setName(ts.getName());
        Optional<TargetSystem> tsQuery = targetSystemRepository.findOne(Example.of(example));

        if (tsQuery.isEmpty()) {
            // Create a new TargetSystem
            TargetSystem targetSystem = new TargetSystem();

            // Parse model files and create task set
            Set<Task> taskSet = new HashSet<Task>();

            // Phase 1 Models
            for (String modelLocation : ts.getPhase1_models_location()) {
                // Extract task names
                List<String> taskNames = modelProcessingService.extractServiceTaskNames(modelLocation);
                for (String taskName : taskNames) {
                    Task task = new Task();
                    task.setId(taskName);
                    taskSet.add(task);

                    if (!taskRepository.findOne(Example.of(task)).isPresent()) {
                        taskRepository.save(task);
                    }
                }
            }

            // Phase 2 Model
            String phase2_model_location = ts.getPhase2_model_location();
            List<String> taskNames = modelProcessingService.extractServiceTaskNames(phase2_model_location);

            for (String taskName : taskNames) {
                Task task = new Task();
                task.setId(taskName);
                taskSet.add(task);
                
                if (!taskRepository.findOne(Example.of(task)).isPresent()) {
                    taskRepository.save(task);
                }
            }

            // Save set to task repository
            taskRepository.saveAll(taskSet);

            // Add tasks to the target system
            targetSystem.setTasks(taskSet);

            // Create name
            targetSystem.setName(ts.getName());

            // Create ontology path
            String ontologyLocation = ontologiesMounpoint + "/"
                    + modelProcessingService.extractLastTaksId(phase2_model_location) + ".owl";
            targetSystem.setOntologyPath(ontologyLocation);

            // Extract ontology dependencies
            Set<OntologyDependency> ontologyDependencies = modelProcessingService
                    .extractOntologyDependencies(ontologyLocation);

            for (OntologyDependency ont : ontologyDependencies) {
                // Check if the ontology dependeicy is already present in te DB and save it if not
                if (ontologyDependencyRepository.findOne(Example.of(ont)).isEmpty()) {
                    ontologyDependencyRepository.save(ont);
                }
            }
            
            // Set the ontology dependencies 
            targetSystem.setOntologyDependencies(ontologyDependencies);

            // Save created target system in DB
            Long targetSystemId = targetSystemRepository.save(targetSystem).getId();
            log.debug("Created new target system with id {}", targetSystemId);

            response.put("status", "Target system created");
            response.put("id", Long.toString(targetSystemId));
            statusCode = HttpStatus.OK;
        } else {
            log.debug("A target system named " + ts.getName() + " already exists. Rejecting");
            response.put("status", "A target system with this name already exists");
            statusCode = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<Map<String, String>>(response, statusCode);
    }

    // Upload method with specific directory
    @PostMapping(path = "/uploadToDir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadToFtpDir(@RequestParam("file") MultipartFile file,
            @RequestParam("location") String location) throws IOException {
        String path = ftpClientService.createDirectory(location, "upload");
        String fileLocation = ftpClientService.uploadFile(file.getOriginalFilename(), path, file.getInputStream());

        Map<String, String> response = new HashMap<String, String>();
        response.put("location", ftpAddress + "/" + fileLocation);

        return new ResponseEntity<Map<String, String>>(response, HttpStatus.OK);
    }

    // Upload method to default ftp dir
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadToFtp(@RequestParam("file") MultipartFile file)
            throws IOException {
        ftpClientService.uploadFile(file.getOriginalFilename(), file.getInputStream());

        Map<String, String> response = new HashMap<String, String>();
        response.put("location", ftpAddress + "/upload/" + file.getOriginalFilename());

        return new ResponseEntity<Map<String, String>>(response, HttpStatus.OK);
    }
}
