package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;

import jakarta.annotation.PostConstruct;

/** Stores blog posts in studentUsers.blogs (the database the student backend reads). */
@Repository
public class BlogRepository {

    private static final Logger log = LoggerFactory.getLogger(BlogRepository.class);

    private final MongoCollection<Document> collection;

    public BlogRepository(MongoClient mongoClient,
                          @Value("${blogs.database:studentUsers}") String database,
                          @Value("${blogs.collection:blogs}") String collectionName) {
        this.collection = mongoClient.getDatabase(database).getCollection(collectionName);
    }

    /** Creates the collection (if missing) and the unique index on slug. Safe to run on every start. */
    @PostConstruct
    void ensureIndexes() {
        try {
            collection.createIndex(Indexes.ascending("slug"),
                    new IndexOptions().unique(true).name("slug_unique"));
        } catch (Exception e) {
            log.error("Could not create unique index on blogs.slug: {}", e.getMessage());
        }
    }

    public List<Document> findAll() {
        return collection.find().sort(Sorts.descending("updatedAt")).into(new ArrayList<>());
    }

    public Optional<Document> findById(ObjectId id) {
        return Optional.ofNullable(collection.find(eq("_id", id)).first());
    }

    public ObjectId insert(Document doc) {
        collection.insertOne(doc);
        return doc.getObjectId("_id");
    }

    public boolean replace(ObjectId id, Document doc) {
        return collection.replaceOne(eq("_id", id), doc).getMatchedCount() > 0;
    }

    public boolean delete(ObjectId id) {
        return collection.deleteOne(eq("_id", id)).getDeletedCount() > 0;
    }
}