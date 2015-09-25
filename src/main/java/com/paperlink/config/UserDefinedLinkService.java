package com.paperlink.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDefinedLinkService {

    @Autowired
    private UserDefinedLinkList userDefinedLinkList;

    public List<String> getUserDict() {
        return userDefinedLinkList.getWords();
    }
}
