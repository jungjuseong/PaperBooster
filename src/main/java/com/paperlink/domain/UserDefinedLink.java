package com.paperlink.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDefinedLink {
	String name;
	String url;
	String resource;

	public UserDefinedLink(String name, String url, String resource) {
		this.name = name;
		this.url = url;

		//if ()
		this.resource = resource;

	}
}
