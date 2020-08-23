package com.ctol.bench.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ctol.bench.model.ExcelHeader;

public interface HeaderRepository extends MongoRepository<ExcelHeader, String>{

}
