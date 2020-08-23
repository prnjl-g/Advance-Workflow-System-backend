package com.ctol.bench;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ctol.bench.property.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class FileApplication {
	public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
