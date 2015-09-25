package com.paperlink.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobProfileService {

    @Autowired
    private JobProfiles jobProfileSettings;

    public JobProfiles getJob() {
        return jobProfileSettings;
    }
}
