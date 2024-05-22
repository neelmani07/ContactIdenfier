package com.example.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.el.stream.Optional;
import org.hibernate.mapping.List;
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

    @Transactional
    public ContactResponse identifyContact(String phoneNumber, String email) {
        Optional<Contact> contactByPhone = contactRepository.findByPhoneNumber(phoneNumber);
        Optional<Contact> contactByEmail = contactRepository.findByEmail(email);

        Contact primaryContact = null;
        if (contactByPhone.isPresent()) {
            primaryContact = getPrimaryContact(contactByPhone.get());
        } else if (contactByEmail.isPresent()) {
            primaryContact = getPrimaryContact(contactByEmail.get());
        }

        if (primaryContact == null) {
            Contact newContact = createNewPrimaryContact(phoneNumber, email);
            return buildContactResponse(newContact);
        } else {
            Contact newSecondaryContact = createNewSecondaryContact(primaryContact, phoneNumber, email);
            return buildContactResponse(primaryContact);
        }
    }

    private Contact getPrimaryContact(Contact contact) {
        if (contact.getLinkPrecedence() == LinkPrecedence.PRIMARY) {
            return contact;
        } else {
            return contactRepository.findById(contact.getLinkedId()).orElse(null);
        }
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

    private Contact createNewSecondaryContact(Contact primaryContact, String phoneNumber, String email) {
        Contact contact = new Contact();
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setLinkedId(primaryContact.getId());
        contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setCreatedAt(LocalDateTime.now());
        return contactRepository.save(contact);
    }

    private ContactResponse buildContactResponse(Contact primaryContact) {
        List<Contact> relatedContacts = contactRepository.findAllByLinkedIdOrId(primaryContact.getId(), primaryContact.getId());

        List<String> emails = relatedContacts.stream().map(Contact::getEmail).distinct().filter(Objects::nonNull).collect(Collectors.toList());
        List<String> phoneNumbers = relatedContacts.stream().map(Contact::getPhoneNumber).distinct().filter(Objects::nonNull).collect(Collectors.toList());
        List<Integer> secondaryContactIds = relatedContacts.stream().filter(c -> c.getLinkPrecedence() == LinkPrecedence.SECONDARY).map(Contact::getId).collect(Collectors.toList());

        return new ContactResponse(primaryContact.getId(), emails, phoneNumbers, secondaryContactIds);
    }
}
