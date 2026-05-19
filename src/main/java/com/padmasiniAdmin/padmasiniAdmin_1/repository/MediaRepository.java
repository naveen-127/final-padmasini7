// repository/MediaRepository.java
package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.MediaItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MediaRepository extends MongoRepository<MediaItem, String> {
    List<MediaItem> findByType(String type);
    void deleteByS3Key(String s3Key);
}