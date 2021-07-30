package de.fraunhofer.iosb.svs.spc;

import java.util.List;

public class TargetSystemGenReq {
    private String name;
    private String key;
    private List<String> phase1_models_location;
    private String phase2_model_location;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getPhase1_models_location() {
        return phase1_models_location;
    }

    public void setPhase1_models_location(List<String> phase1_models_location) {
        this.phase1_models_location = phase1_models_location;
    }

    public String getPhase2_model_location() {
        return phase2_model_location;
    }

    public void setPhase2_model_location(String phase2_model_location) {
        this.phase2_model_location = phase2_model_location;
    }
}
