package de.sammy8806.tests.testasynctasks;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AsyncBackend {

    final ConcurrentHashMap<String, JobMeta<?>> jobMap = new ConcurrentHashMap<>(5);

    public JobMeta<?> getJob(String id) {
        JobMeta<?> jobMeta;

        log.trace("Getting job {}", id);

        synchronized (this) {
            jobMeta = jobMap.get(id);
        }

        log.trace("Found Job: {} ; Status: {}", id, jobMeta.getStatus());
        return jobMeta;
    }

    public synchronized Map<String, JobMeta<?>> getJobMap() {
        return new HashMap<>(jobMap);
    }

    @Async
    public <T> JobMeta<T> runJob(JobMeta.JobTask<T> task) {
        JobMeta<T> jobMeta = (JobMeta<T>) JobMeta.builder()
            .id(UUID.randomUUID().toString())
            .creationTime(Instant.now())
            .executorMethod((JobMeta.JobTask<Object>) task)
            .future(CompletableFuture.supplyAsync((Supplier<Object>) task))
            .build();

        persistJob(jobMeta);

        return jobMeta;
    }

    protected void persistJob(JobMeta<?> meta) {
        if (jobMap.containsKey(meta.getId())) {
            throw new RuntimeException("JobId already exists, please try another one");
        }

        synchronized (this) {
            jobMap.put(meta.getId(), meta);
        }
    }

    enum Status {
        STARTED,
        FINISHED,
        FAILED
    }

}
