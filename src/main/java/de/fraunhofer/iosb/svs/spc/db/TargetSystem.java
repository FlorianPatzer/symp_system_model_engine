package de.fraunhofer.iosb.svs.spc.db;


import javax.persistence.*;
import java.util.Set;

@Entity
public class TargetSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String name;
    @ManyToMany
    @JoinTable(
            name = "targetsystems_tasks",
            joinColumns = @JoinColumn(name = "targetsystem_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Set<Task> tasks;
    @ManyToMany
    @JoinTable(
            name = "targetsystems_ontologydependencies",
            joinColumns = @JoinColumn(name = "targetsystem_id"),
            inverseJoinColumns = @JoinColumn(name = "ontologydependency_id")
    )
    private Set<OntologyDependency> ontologyDependencies;

    /**
     * If ontologyPath is null --> not yet ready
     */
    @Column(nullable = true)
    private String ontologyPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Set<OntologyDependency> getOntologyDependencies() {
        return ontologyDependencies;
    }

    public void setOntologyDependencies(Set<OntologyDependency> ontologyDependencies) {
        this.ontologyDependencies = ontologyDependencies;
    }

    public String getOntologyPath() {
        return ontologyPath;
    }

    public void setOntologyPath(String ontologyPath) {
        this.ontologyPath = ontologyPath;
    }
}
