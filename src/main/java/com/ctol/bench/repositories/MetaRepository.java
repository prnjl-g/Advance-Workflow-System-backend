package com.ctol.bench.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ctol.bench.model.MetaData;

public interface MetaRepository extends MongoRepository<MetaData, String> {

}
