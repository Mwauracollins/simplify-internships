package org.simplifyinternships.simplifyinternships.repositories;

import org.simplifyinternships.simplifyinternships.entities.Experience;
import org.simplifyinternships.simplifyinternships.entities.userentities.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Integer> {
    @Override
    Optional<Experience> findById(Integer integer);
    List<Experience> findByUser(BaseUser user);
    Experience findByUserAndCompanyName(BaseUser user, String companyName);

    @Override
    List<Experience> findAll();
}
