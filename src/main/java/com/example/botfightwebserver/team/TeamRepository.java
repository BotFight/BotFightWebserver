package com.example.botfightwebserver.team;

import jakarta.transaction.Transactional;
import org.hibernate.envers.AuditReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByName(String name);
    Page<Team> findAllByOrderByGlickoDesc(Pageable pageable);
}
