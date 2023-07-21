package com.dguntha.personalapis;

import com.dguntha.personalapis.utils.Constants;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class PersonalApiApplication {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Value("${ttl.index.jobLog}")
	private long ttlIndexForJobLog;

	public static void main(String[] args) {
		SpringApplication.run(PersonalApiApplication.class, args);
	}

	@PostConstruct
	public void createCollectionWithTTLIndex() {
		String collectionName = Constants.COLL_JOB_LOG_TRANSACTION;

		if (!mongoTemplate.collectionExists(collectionName)) {
			mongoTemplate.createCollection(collectionName);
		}
		IndexOperations indexOps = mongoTemplate.indexOps(collectionName);
		Index index = new Index().expire(50, TimeUnit.DAYS);
		indexOps.ensureIndex(index.named("expiry_index").on("_ts", Sort.Direction.ASC));




	}
}
