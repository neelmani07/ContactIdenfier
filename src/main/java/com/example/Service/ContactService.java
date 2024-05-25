package com.example.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Entity.Contact;
import com.example.Entity.LinkPrecedence;
import com.example.Repository.ContactRepository;
import com.example.models.ContactResponse;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public ContactResponse identifyContact(String phoneNumber, String email) {
        // Find contacts by email and phone number
        Optional<Contact> contactByEmail = Optional.empty();
        Optional<Contact> contactByPhone = Optional.empty();
        
        if (email != null) {
            contactByEmail = contactRepository.findFirstByEmail(email);
        }
        
        if (phoneNumber != null) {
            contactByPhone = contactRepository.findFirstByPhoneNumber(phoneNumber);
        }

        Contact primaryContact;
        
        if (contactByEmail.isPresent() && contactByPhone.isPresent()) {
            Contact emailContact = contactByEmail.get();
            Contact phoneContact = contactByPhone.get();
            if (emailContact.getId().equals(phoneContact.getId())) {
                primaryContact = emailContact;
            } else {
                // Determine which contact is older
                primaryContact = emailContact.getCreatedAt().isBefore(phoneContact.getCreatedAt()) ? emailContact : phoneContact;
                Contact secondaryContact = emailContact.getCreatedAt().isAfter(phoneContact.getCreatedAt()) ? emailContact : phoneContact;
                secondaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                secondaryContact.setLinkedId(primaryContact.getId());
                contactRepository.save(secondaryContact);
            }
        } else if (contactByEmail.isPresent()) {
            primaryContact = contactByEmail.get();
            createNewSecondaryContact(primaryContact, phoneNumber, email);
        } else if (contactByPhone.isPresent()) {
            primaryContact = contactByPhone.get();
            createNewSecondaryContact(primaryContact, phoneNumber, email);
        } else {
            primaryContact = createNewPrimaryContact(phoneNumber, email);
        }
        return buildContactResponse(primaryContact);
    }

    private Contact createNewPrimaryContact(String phoneNumber, String email) {
        Contact contact = new Contact();
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setLinkPrecedence(LinkPrecedence.PRIMARY);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return contactRepository.save(contact);
    }

    private void createNewSecondaryContact(Contact primaryContact, String phoneNumber, String email) {
    	if(phoneNumber==null || email==null)return;
        Contact contact = new Contact();
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setLinkedId(primaryContact.getId());
        contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setCreatedAt(LocalDateTime.now());
        contactRepository.save(contact);
    }

    private ContactResponse buildContactResponse(Contact primaryContact) {
        List<Contact> relatedContacts = contactRepository.findAllByLinkedIdOrId(primaryContact.getId(), primaryContact.getId());

        List<String> emails = new ArrayList<>();
        List<String> phoneNumbers = new ArrayList<>();
        List<Integer> secondaryContactIds = new ArrayList<>();

        Set<String> emailSet = new HashSet<>();
        Set<String> phoneNumberSet = new HashSet<>();

        for (Contact contact : relatedContacts) {
            if (contact.getEmail() != null && emailSet.add(contact.getEmail())) {
                emails.add(contact.getEmail());
            }
            if (contact.getPhoneNumber() != null && phoneNumberSet.add(contact.getPhoneNumber())) {
                phoneNumbers.add(contact.getPhoneNumber());
            }
            if (contact.getLinkPrecedence() == LinkPrecedence.SECONDARY) {
                secondaryContactIds.add(contact.getId());
            }
        }

        return new ContactResponse(primaryContact.getId(), emails, phoneNumbers, secondaryContactIds);
    }

}
