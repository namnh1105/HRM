package com.hainam.worksphere.shared.persistence.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import java.time.format.DateTimeParseException;

import java.time.LocalDate;

@Slf4j
@Component
@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {

    private static AesGcmStringEncryptor encryptor;

    @Autowired
    public void setEncryptor(AesGcmStringEncryptor encryptor) {
        EncryptedLocalDateConverter.encryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        if (encryptor == null) {
            throw new IllegalStateException("EncryptedLocalDateConverter is not initialized");
        }
        return encryptor.encrypt(attribute.toString());
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        if (encryptor == null) {
            log.warn("EncryptedLocalDateConverter is not initialized yet. Trying to parse raw dbData as LocalDate.");
            try {
                return LocalDate.parse(dbData);
            } catch (DateTimeParseException e) {
                log.error("Failed to parse raw dbData as LocalDate: {}", dbData);
                return null;
            }
        }
        String plainDate = encryptor.decrypt(dbData);
        if (plainDate == null) {
            return null;
        }
        try {
            return LocalDate.parse(plainDate);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse decrypted date '{}' for dbData '{}'. Error: {}", plainDate, dbData, e.getMessage());
            return null;
        }
    }
}
