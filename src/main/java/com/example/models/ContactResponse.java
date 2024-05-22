package com.example.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public
class ContactResponse {
    private Integer primaryContactId;
    private List<String> emails;
    private List<String> phoneNumbers;
    private List<Integer> secondaryContactIds;

    public ContactResponse(Integer primaryContactId, List<String> emails, List<String> phoneNumbers, List<Integer> secondaryContactIds) {
        this.primaryContactId = primaryContactId;
        this.emails = emails;
        this.phoneNumbers = phoneNumbers;
        this.secondaryContactIds = secondaryContactIds;
    }
}