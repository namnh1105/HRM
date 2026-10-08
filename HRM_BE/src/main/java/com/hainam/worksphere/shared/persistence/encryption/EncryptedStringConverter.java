package com.hainam.worksphere.shared.persistence.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static AesGcmStringEncryptor encryptor;

    @Autowired
    public void setEncryptor(AesGcmStringEncryptor encryptor) {
        EncryptedStringConverter.encryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (encryptor == null) {
            throw new IllegalStateException("EncryptedStringConverter is not initialized");
        }
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptor == null) {
            log.warn("EncryptedStringConverter is not initialized yet. Returning raw dbData.");
            return dbData;
        }
        return encryptor.decrypt(dbData);
    }
}
