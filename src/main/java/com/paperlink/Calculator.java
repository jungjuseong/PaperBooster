package com.paperlink;

import java.util.List;

import com.paperlink.config.JobProfileService;
import com.paperlink.domain.UserDefinedLink;

public interface Calculator {
	int doJob(JobProfileService jobProfileService, List<UserDefinedLink> eachDict, int textColor, int backgroundColor);
	void doScan(JobProfileService jobProfileService);
	void showInfo(JobProfileService jobProfileService);
}
