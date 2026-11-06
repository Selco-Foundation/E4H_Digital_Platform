package org.egov.amc.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.DecryptionRequest;
import org.egov.amc.web.models.EncryptObject;
import org.egov.amc.web.models.Facility;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decrypts {@code facility_poc_phone} values stored encrypted in the facility table,
 * matching health-facility-registry FacilityService.decryptMobileNumber().
 */
@Slf4j
@Component
public class FacilityPocPhoneUtil {

    private final EncryptionDecryptionUtil encryptionDecryptionUtil;

    public FacilityPocPhoneUtil(EncryptionDecryptionUtil encryptionDecryptionUtil) {
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
    }

    public void decryptPocPhoneIfPresent(Facility facility) {
        if (facility == null) {
            return;
        }
        String encryptedPhone = facility.getFacilityPocPhone();
        if (encryptedPhone == null || encryptedPhone.isBlank()) {
            return;
        }
        try {
            String decryptedMobileNumber = decryptMobileNumber(encryptedPhone);
            if (decryptedMobileNumber != null && !decryptedMobileNumber.isBlank()) {
                facility.setFacilityPocPhone(decryptedMobileNumber);
            }
        } catch (Exception e) {
            log.trace("Decrypt POC phone skipped for facility {}", facility.getId());
        }
    }

    /**
     * True when {@code value} carries egov-enc-service's {@code keyId|ciphertext} envelope, i.e. it
     * is safe to hand to the decrypt endpoint. A bare mobile number returns false — passing one to
     * {@code _decrypt} makes egov-enc-service throw {@code "<value>: Invalid Ciphertext"}.
     */
    public static boolean isEncrypted(String value) {
        if (value == null) {
            return false;
        }
        int separator = value.indexOf('|');
        if (separator <= 0 || separator == value.length() - 1) {
            return false;
        }
        return value.substring(0, separator).chars().allMatch(Character::isDigit);
    }

    public String decryptMobileNumber(String mobileNumber) {
        if (!isEncrypted(mobileNumber)) {
            return mobileNumber;
        }
        String decryptedMobileNumber = null;
        if (mobileNumber != null && !mobileNumber.isBlank()) {
            EncryptObject object = EncryptObject.builder()
                    .mobileNumber(mobileNumber)
                    .build();
            Map<String, EncryptObject> userMap = new HashMap<>();
            userMap.put("userObject", object);
            DecryptionRequest request = DecryptionRequest.builder()
                    .decryptionRequests(List.of(userMap))
                    .build();
            List<Map<String, EncryptObject>> response = encryptionDecryptionUtil.decryptObject(request);
            for (Map<String, EncryptObject> map : response) {
                EncryptObject user = map.get("userObject");
                if (user != null) {
                    decryptedMobileNumber = user.getMobileNumber();
                }
            }
        }
        return decryptedMobileNumber;
    }
}
