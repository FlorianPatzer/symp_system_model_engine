package de.fraunhofer.iosb.svs.spc.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TargetSystemRepository extends JpaRepository<TargetSystem, Long> {

    Optional<TargetSystem> findByName(String name);

}
