package com.paperlink.domain;

// TextLink는 단어에 대한 링크 정보를 담는다.

public class TextLink {
	private String queryString;
    private String linkType;
    private String linkURL;
    
    private int start;
    private int end; // glyph index

    public TextLink(String queryString, String linkType, String linkURL, int start, int end) {
    	this.queryString = queryString;
        this.linkType = linkType;
        this.linkURL = linkURL;

        this.start = start;
        this.end = end;
    }

    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String queryString) {
    	this.queryString = queryString;
    }
    
    public String getLinkType() {
        return linkType;
    }

    public String getLinkURL() {
        return linkURL;
    }
	
	public int getStart() { return this.start; }
	public int getEnd() { return this.end; }
}