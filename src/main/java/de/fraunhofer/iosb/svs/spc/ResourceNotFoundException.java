package de.fraunhofer.iosb.svs.spc;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String item, String id) {
        super("No '" + item + "' with id '" + id + "' found");
    }

    public ResourceNotFoundException(String item, Long id) {
        this(item, String.valueOf(id));
    }
}