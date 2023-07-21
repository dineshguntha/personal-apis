package com.dguntha.personalapis.controller;

import com.dguntha.personalapis.model.entity.JobLogEntity;
import com.dguntha.personalapis.services.JobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/joblog")
public class JobLogController {

    private final JobLogService jobLogService;

    @GetMapping("/id")
    public JobLogEntity getByJobLogId(@RequestParam(value = "id") String id)
    {
        return jobLogService.findJobLogById(id);
    }


    @PostMapping("/save")
    public JobLogEntity saveJobLog(@RequestBody JobLogEntity jobLogEntity)
    {
       return jobLogService.saveJobLog(jobLogEntity);
    }

    @PostMapping("/update")
    public JobLogEntity updateJobLog(@RequestBody JobLogEntity updateJobLogEntity)
    {
        return jobLogService.updateJobLog(updateJobLogEntity);
    }

    @GetMapping("/list")
    public List<JobLogEntity> list()
    {
        return jobLogService.listOfJobLogs();
    }

    @DeleteMapping("/JobLogDeletionByID/{id}")
    public ResponseEntity<String> jobLogDeletionById(@PathVariable String id)
    {
        jobLogService.deleteByID(id);
        return ResponseEntity.ok("Resource deleted successfully");
    }
}
