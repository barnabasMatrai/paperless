package org.example.paperless.repository;

import org.example.paperless.entity.Reminder;
import org.example.paperless.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
}
