package com.example.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentifyRequest {
	private String phoneNumber;
    private String email;
}