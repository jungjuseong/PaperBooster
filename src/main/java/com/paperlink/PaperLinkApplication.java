package com.paperlink;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.paperlink.config.JobProfileService;
import com.paperlink.config.UserDefinedLinkService;
import com.paperlink.domain.Pantone;
import com.paperlink.service.PantoneService;
import com.paperlink.domain.UserDefinedLink;

@SpringBootApplication
public class PaperLinkApplication implements CommandLineRunner {

	@Autowired
	Calculator calc2;

	@Autowired
	JobProfileService jobProfileService;

	@Autowired
	PantoneService pantoneService;

	@Autowired
	UserDefinedLinkService userdictService;
	
	static final Logger logger = LogManager.getFormatterLogger(PaperLinkApplication.class.getName());
	
	private void showUserDict() {
		
		for (String word : userdictService.getUserDict()) 
			System.out.println(word);
	}
	
	private int convertPantoneToInt(Pantone pantone) {        
        
        return  (pantone.getCyan() << 24)    & 0xff000000 |
        		(pantone.getMagenta() << 16) & 0x00ff0000 |
        		(pantone.getYellow() << 8)   & 0x0000ff00 |
        		(pantone.getBlack())         & 0x000000ff;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(PaperLinkApplication.class, args);
	}
	
	@Override
	public void run(String... strings) throws Exception {

		logger.trace("AutoConfiguration should have wired up our stuff.");
		logger.trace("Let's see if we are job-profile-worthy...\n");

       
        List<UserDefinedLink> userDefinedLinklist = new ArrayList<UserDefinedLink>();
        if (userdictService.getUserDict() != null) {
        	showUserDict();        	
        	
        	for (String token : userdictService.getUserDict()) {
        		String splits[] = token.split(",");
        		
        		if (splits.length == 3)
        			userDefinedLinklist.add(new UserDefinedLink(splits[0], splits[1], splits[2]));
        		else if (splits.length == 2)
        			userDefinedLinklist.add(new UserDefinedLink(splits[0], splits[1], ""));
        		else if (splits.length == 1)
        			userDefinedLinklist.add(new UserDefinedLink(splits[0], "", ""));

        	}
        }
        if (jobProfileService.getJob() == null) {

        	logger.error("No JobProfile for us!");
            
            return;
        }
	
        String textColorName = jobProfileService.getJob().getTextColor();
        Pantone textColor = pantoneService.findOne(textColorName);
        
        System.out.println(textColorName + ": " + textColorName);
        
        int textColorInt = convertPantoneToInt(pantoneService.findOne(jobProfileService.getJob().getTextColor()));
        int backgroundColor = convertPantoneToInt(pantoneService.findOne(jobProfileService.getJob().getBackgroundColor()));
        
        //calc.showInfo(jobProfileService);
        //calc.doScan(jobProfileService);

        calc2.doJob(jobProfileService, userDefinedLinklist, textColorInt, backgroundColor);
	}
	


}


