package de.sammy8806.tests.testasynctasks;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
public class TestAsyncTasksApplication {

    @Autowired
    AsyncBackend asyncBackend;

    public static void main(String[] args) {
        SpringApplication.run(TestAsyncTasksApplication.class, args);
    }

    @GetMapping
    public Object runTask() throws InterruptedException, ExecutionException {
        log.info("runTask");

        JobMeta<String> meta = asyncBackend.runJob(() -> {
            try {
                log.info("lambda: started");
                Thread.sleep(1000);
                throw new RuntimeException("something went wrong");
                // Thread.sleep(1000);
                // log.info("lambda: ended");
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }

            return "THIS HAS RUN!";
        });

        reportStatus(meta);

        Thread.sleep(3000);
        log.info("Waiting 3s");

        reportStatus(meta);

        if (meta.getStatus() != AsyncBackend.Status.STARTED) {
            String data;

            try {
                data = meta.getResult();
            } catch (ExecutionException e) {
                log.warn("Task {} failed!", meta.getId(), e);
                return e;
            }

            log.info("RESULT: {}", data);
        }

        return null;
    }

    void reportStatus(JobMeta<?> meta) throws ExecutionException, InterruptedException {
        AsyncBackend.Status status = meta.getStatus();
        log.info("Task {} status: {} {}", meta.getId(), status, status == AsyncBackend.Status.FINISHED ? "result: " + meta.getResult() : "");
    }

}
