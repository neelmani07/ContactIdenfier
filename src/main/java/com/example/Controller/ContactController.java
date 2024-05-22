package com.example.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Service.ContactService;
import com.example.models.IdentifyRequest;
import com.example.models.ContactResponse;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/identify")
    public ResponseEntity<ContactResponse> identify(@RequestBody IdentifyRequest request) {
        ContactResponse contactResponse = contactService.identifyContact(request.getPhoneNumber(), request.getEmail());
        return ResponseEntity.ok(contactResponse);
    }
}
