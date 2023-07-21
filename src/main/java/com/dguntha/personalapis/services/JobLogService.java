package com.dguntha.personalapis.services;

import com.dguntha.personalapis.exception.DocumentIdNotFoundException;
import com.dguntha.personalapis.exception.DocumentNotPresentException;
import com.dguntha.personalapis.repository.JobLogRepository;
import com.dguntha.personalapis.model.entity.JobLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobLogService {
    private final JobLogRepository jobLogRepository;

    public JobLogEntity findJobLogById(String id) {
        return jobLogRepository.findById(id).orElse(null);
    }

    public JobLogEntity saveJobLog(JobLogEntity jobLogEntity)
    {
       return  jobLogRepository.save(jobLogEntity);
    }


    public JobLogEntity updateJobLog(JobLogEntity jobLogEntity)
    {
        if (jobLogEntity.getId() == null || findJobLogById(jobLogEntity.getId()) == null)
            throw new DocumentNotPresentException("Job Log is not found");

        return jobLogRepository.save(jobLogEntity);
    }


    public List<JobLogEntity> listOfJobLogs()
    {
        return jobLogRepository.findAll();
    }


    public void deleteByID(String id)
    {
        if (findJobLogById(id) == null)
            throw new DocumentIdNotFoundException("Job log is not found "+id);

        jobLogRepository.deleteById(id);
    }

}
