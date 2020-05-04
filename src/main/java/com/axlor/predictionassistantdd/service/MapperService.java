package com.axlor.predictionassistantdd.service;

import com.axlor.predictionassistantdd.model.Snapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Date;


/**
 * Service in charge of mapping a Json formatted String to a Snapshot object.
 */
@Service
public class MapperService {

    /**
     * Maps the input String to a Snapshot object using Jackson if possible.
     *
     * @param json A Json formatted string representing all current market data from PredictIt's API
     * @return Returns a Snapshot object if the input param was correctly formatted Json for this. Returns null if mapping fails.
     */
    Snapshot mapToSnapshot(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Snapshot snapshot = objectMapper.readValue(json, new TypeReference<Snapshot>(){});
            snapshot.setHashId(json.hashCode());
            snapshot.setTimestamp(System.currentTimeMillis());
            return snapshot;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
