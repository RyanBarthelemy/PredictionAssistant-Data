package com.axlor.predictionassistantdd.resository;

import com.axlor.predictionassistantdd.model.Snapshot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A simple repository for persisting Snapshots to the database.
 * Sets class type to persist to Snapshot.class and informs that primary key to use is of type Integer.
 */
@Repository                                  //<Entity's class, primaryKey type>
public interface SnapshotRepository extends CrudRepository<Snapshot, Integer> {

}
