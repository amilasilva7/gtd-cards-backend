package org.ostech.gtdcardsbackend.repository;

import org.ostech.gtdcardsbackend.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}
