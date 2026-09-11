package com.chhavi.firstjobapp.company;

import com.chhavi.firstjobapp.job.Job;

import java.util.List;

public interface CompanyService {
    List<Company> getAllCompanies();
    Boolean updateCompany(Company company,Long id);
    void createCompany(Company company);

    boolean deleteCompanyById(Long id);


    Company CompanyById(Long id);
}
