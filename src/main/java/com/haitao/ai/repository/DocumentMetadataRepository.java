package com.haitao.ai.repository;

import com.haitao.ai.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {

    Optional<DocumentMetadata> findByDocumentId(String documentId);
    
    @Query("SELECT d FROM DocumentMetadata d WHERE d.documentId = :documentId AND d.deleted = false")
    Optional<DocumentMetadata> findActiveByDocumentId(String documentId);
    
    @Modifying
    @Query("UPDATE DocumentMetadata d SET d.deleted = true WHERE d.documentId = :documentId")
    void softDeleteByDocumentId(String documentId);
    
    boolean existsByDocumentId(String documentId);
}