package facility.util;

import facility.config.Configuration;
import facility.web.models.DecryptionRequest;
import facility.web.models.EncReqObject;
import facility.web.models.EncryptObject;
import facility.web.models.EncryptionRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single entry point for encrypting / decrypting POC mobile numbers via egov-enc-service.
 *
 * <p>Both operations are idempotent: {@link #encrypt} leaves an already-encrypted value alone and
 * {@link #decrypt} leaves an already-plaintext value alone. That guard matters because the same
 * value can reach a write path either straight off a search result (decrypted) or straight off the
 * facility table (encrypted) — see {@code FacilityRepository}, which encrypts on every push.
 *
 * <p>Without it, decrypting a plaintext value makes egov-enc-service throw
 * {@code "<value>: Invalid Ciphertext"} from {@code Ciphertext.<init>}, because it expects the
 * {@code keyId|ciphertext} envelope.
 */
@Slf4j
@Component
public class PocPhoneCipher {

    private static final String USER_OBJECT_KEY = "userObject";
    private static final String ENCRYPTION_TYPE = "Normal";

    private final EncryptionDecryptionUtil encryptionDecryptionUtil;
    private final Configuration configs;

    public PocPhoneCipher(EncryptionDecryptionUtil encryptionDecryptionUtil, Configuration configs) {
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.configs = configs;
    }

    /**
     * True when {@code value} carries egov-enc-service's {@code keyId|ciphertext} envelope, i.e. it
     * is safe to hand to the decrypt endpoint. A bare mobile number returns false.
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Encrypts a plaintext mobile number, returning {@code value} unchanged when it is blank or
     * already encrypted.
     *
     * @throws CustomException if egov-enc-service cannot be reached or returns nothing usable —
     *         callers must not fall back to persisting the plaintext number.
     */
    public String encrypt(String value) {
        if (isBlank(value) || isEncrypted(value)) {
            return value;
        }
        String encrypted;
        try {
            EncReqObject encReqObject = EncReqObject.builder()
                    .tenantId(configs.getEncServiceTenantId())
                    .type(ENCRYPTION_TYPE)
                    .value(wrap(value))
                    .build();
            EncryptionRequest request = EncryptionRequest.builder()
                    .encryptionRequests(List.of(encReqObject))
                    .build();
            encrypted = unwrap(encryptionDecryptionUtil.encryptObject(request));
        } catch (Exception e) {
            throw new CustomException("POC_PHONE_ENCRYPTION_FAILED",
                    "Could not encrypt POC mobile number: " + e.getMessage());
        }
        if (isBlank(encrypted)) {
            throw new CustomException("POC_PHONE_ENCRYPTION_FAILED",
                    "egov-enc-service returned no ciphertext for the POC mobile number");
        }
        return encrypted;
    }

    /**
     * Decrypts an encrypted mobile number, returning {@code value} unchanged when it is blank, not
     * in ciphertext form, or when egov-enc-service fails. Reads stay best-effort — a legacy
     * plaintext row should still be readable.
     */
    public String decrypt(String value) {
        if (isBlank(value) || !isEncrypted(value)) {
            return value;
        }
        try {
            DecryptionRequest request = DecryptionRequest.builder()
                    .decryptionRequests(List.of(wrap(value)))
                    .build();
            String decrypted = unwrap(encryptionDecryptionUtil.decryptObject(request));
            return isBlank(decrypted) ? value : decrypted;
        } catch (Exception e) {
            log.error("Could not decrypt POC mobile number, returning stored value as-is: {}", e.getMessage());
            return value;
        }
    }

    private Map<String, EncryptObject> wrap(String mobileNumber) {
        Map<String, EncryptObject> userMap = new HashMap<>();
        userMap.put(USER_OBJECT_KEY, EncryptObject.builder().mobileNumber(mobileNumber).build());
        return userMap;
    }

    private String unwrap(List<Map<String, EncryptObject>> response) {
        if (response == null) {
            return null;
        }
        String mobileNumber = null;
        for (Map<String, EncryptObject> map : response) {
            EncryptObject user = map.get(USER_OBJECT_KEY);
            if (user != null) {
                mobileNumber = user.getMobileNumber();
            }
        }
        return mobileNumber;
    }
}
