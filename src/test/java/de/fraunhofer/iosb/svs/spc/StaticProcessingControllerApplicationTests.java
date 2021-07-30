package de.fraunhofer.iosb.svs.spc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iosb.svs.spc.db.TargetSystem;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaticProcessingControllerApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(StaticProcessingControllerApplicationTests.class);

    public static String createdTargetSystemId;

    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testGettingAllTargetSystems() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<Object>(headers);

        ResponseEntity<List<TargetSystem>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/targetsystem/", HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<TargetSystem>>() {
                });

        List<TargetSystem> analyses = response.getBody();
        assertThat(analyses.size(), not(0));
    }

    @SuppressWarnings("unchecked")
    private String handleModelUpload(String modelLocation)
            throws JsonMappingException, JsonProcessingException, URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource(modelLocation));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + "/api/upload",
                requestEntity, String.class);

        Map<String, String> bodyHashMap = new ObjectMapper().readValue(response.getBody(), Map.class);

        String model_ftp_location = bodyHashMap.get("location");
        return model_ftp_location;
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(2)
    public void testCreatingTargetSystem() throws JsonMappingException, JsonProcessingException, URISyntaxException {
        List<String> phase1_models_ftp_location = new ArrayList<String>();
        String phase2_model_ftp_location = "";

        String targetSystemName = "My Test Target System";
        String targetSystemKey = "my-test-target-system";

        List<String> phase1_models_local_path = new ArrayList<String>();
        phase1_models_local_path.add(
                "config_analysis_example/phases/fuse_and_clean/testbed_mini_arch_fc_nac_configuration_analysis.bpmn");
        String phase2_model_local_path = "config_analysis_example/phases/stat_extension/testbed_mini_arch_se_nac_configuration_analysis.bpmn";

        // Upload phase 1 models
        for (String model_path : phase1_models_local_path) {
            String model_ftp_location = handleModelUpload(model_path);
            phase1_models_ftp_location.add(model_ftp_location);
        }

        // Upload phase 2 models
        phase2_model_ftp_location = handleModelUpload(phase2_model_local_path);

        // Register target system
        // Create Request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        TargetSystemGenReq targetSystemGenerationRequest = new TargetSystemGenReq();
        targetSystemGenerationRequest.setName(targetSystemName);
        targetSystemGenerationRequest.setKey(targetSystemKey);
        targetSystemGenerationRequest.setPhase1_models_location(phase1_models_ftp_location);
        targetSystemGenerationRequest.setPhase2_model_location(phase2_model_ftp_location);

        String requestData = new ObjectMapper().writeValueAsString(targetSystemGenerationRequest);

        // Make request
        HttpEntity<String> request = new HttpEntity<String>(requestData, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + "/api/targetsystem",
                request, String.class);

        Map<String, String> bodyHashMap = new ObjectMapper().readValue(response.getBody(), Map.class);
        createdTargetSystemId = bodyHashMap.get("id");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    // @Test
    // TODO: Update target system
    // Feature not implemented

    @Test
    @Order(3)
    public void testGettingTargetSystem() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<Object>(headers);

        ResponseEntity<TargetSystem> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/targetsystem/" + createdTargetSystemId, HttpMethod.GET, entity,
                TargetSystem.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    // @Test
    // TODO: Delete target system

    @Test
    @Order(5)
    public void testDeletingTargetSystem() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<Object>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/targetsystem/" + createdTargetSystemId, HttpMethod.DELETE,
                entity, String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }
}
