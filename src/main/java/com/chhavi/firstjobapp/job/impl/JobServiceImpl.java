package com.chhavi.firstjobapp.job.impl;

import com.chhavi.firstjobapp.company.Company;
import com.chhavi.firstjobapp.company.CompanyRepository;
import com.chhavi.firstjobapp.job.Job;
import com.chhavi.firstjobapp.job.JobRepository;
import com.chhavi.firstjobapp.job.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobServiceImpl implements JobService {

    JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    @Override
    public void createJob(Job job) {
        if (job.getCompany() != null && job.getCompany().getId() != null) {
            Company company = companyRepository.findById(job.getCompany().getId()).orElse(null);
            job.setCompany(company);
        }
        jobRepository.save(job);
    }

    @Override
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteJobById(Long id) {
        try {
            jobRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateJob(Long id, Job updatedJob) {
        Optional<Job> jobOptional = jobRepository.findById(id);
        if (jobOptional.isPresent()) {
            Job job = jobOptional.get();
            job.setTitle(updatedJob.getTitle());
            job.setDescription(updatedJob.getDescription());
            job.setMinSalary(updatedJob.getMinSalary());
            job.setMaxSalary(updatedJob.getMaxSalary());
            job.setLocation(updatedJob.getLocation());
            if (updatedJob.getCompany() != null && updatedJob.getCompany().getId() != null) {
                Company company = companyRepository.findById(updatedJob.getCompany().getId()).orElse(null);
                job.setCompany(company);
            }
            jobRepository.save(job);
            return true;
        }
        return false;
    }
}