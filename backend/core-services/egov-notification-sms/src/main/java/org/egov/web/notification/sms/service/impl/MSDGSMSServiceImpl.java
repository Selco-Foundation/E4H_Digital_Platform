package org.egov.web.notification.sms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.web.notification.sms.config.SMSConstants;
import org.egov.web.notification.sms.config.SMSProperties;
import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(value = "sms.provider.class", matchIfMissing = true, havingValue = "MSDG")
public class MSDGSMSServiceImpl extends BaseSMSService {

    @Autowired
    private SMSProperties smsProperties;

    @Autowired
    private SMSBodyBuilder bodyBuilder;


    /**
     * MD5 encryption algorithm
     *
     * @param text
     * @return
     */
    private static String MD5(String text) {
        log.trace("MD5 encryption method invoked");
        MessageDigest md;
        byte[] md5 = new byte[64];
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            md5 = md.digest();
            log.debug("Password encryption completed, hash length: {}", md5.length);
        } catch (Exception e) {
            log.error("Exception occurred while encrypting password", e);
        }
        return convertedToHex(md5);

    }

    private static String convertedToHex(byte[] data) {
        log.trace("convertedToHex method invoked");
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < data.length; i++) {
            int halfOfByte = (data[i] >>> 4) & 0x0F;
            int twoHalfBytes = 0;

            do {
                if (0 <= halfOfByte && halfOfByte <= 9)
                    buf.append((char) ('0' + halfOfByte));
                else
                    buf.append((char) ('a' + (halfOfByte - 10)));

                halfOfByte = data[i] & 0x0F;

            } while (twoHalfBytes++ < 1);
        }
        log.debug("Hex conversion completed, result length: {}", buf.length());
        return buf.toString();
    }

    protected void submitToExternalSmsService(Sms sms) {
        log.trace("submitToExternalSmsService method invoked for MSDG SMS provider");
        log.info("Processing SMS request via MSDG provider");
        
        String originalMessage = sms.getMessage();
        log.debug("Original message length: {}", originalMessage != null ? originalMessage.length() : 0);
        
        String finalmessage = "";
        for (int i = 0; i < sms.getMessage().length(); i++) {
            char ch = sms.getMessage().charAt(i);
            int j = (int) ch;
            String sss = "&#" + j + ";";
            finalmessage = finalmessage + sss;
        }
        sms.setMessage(finalmessage);
        log.debug("Message converted to HTML entities, final length: {}", finalmessage.length());
        
        String url = smsProperties.getUrl();
        log.debug("MSDG SMS provider URL: {}", url);
        
        final MultiValueMap<String, String> requestBody = bodyBuilder.getSmsRequestBody(sms);
        log.debug("Request body created with {} entries", requestBody.size());
        
        postProcessor(requestBody);
        log.debug("Post-processing completed on request body");
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, getHttpHeaders());
        log.info("Executing POST request to MSDG SMS provider");
        executeAPI(URI.create(url), HttpMethod.POST, request, String.class);
        log.info("MSDG SMS provider request completed successfully");
    }

    /**
     * Performs post processing on the default parameters
     *
     * @param requestBody
     */
    private void postProcessor(MultiValueMap<String, String> requestBody) {
        log.trace("postProcessor method invoked");
        Map<String, String> configMap = getConfigMap();
        log.debug("Config map retrieved with {} entries", configMap.size());
        
        String password = requestBody.getFirst(configMap.get(SMSConstants.SENDER_PASSWORD_IDENTIFIER));
        String username = requestBody.getFirst(configMap.get(SMSConstants.SENDER_USERNAME_IDENTIFIER));
        String senderid = requestBody.getFirst(configMap.get(SMSConstants.SENDER_SENDERID_IDENTIFIER));
        String message = requestBody.getFirst(configMap.get(SMSConstants.SENDER_MESSAGE_IDENTIFIER));
        String secureKey = requestBody.getFirst(configMap.get(SMSConstants.SENDER_SECUREKEY_IDENTIFIER));
        log.debug("Extracted credentials and message from request body");

        String encryptedPwd = MD5(password);
        log.debug("Password encrypted successfully");
        
        String hashMsg = hashGenerator(username, senderid, message, secureKey);
        log.debug("Hash message generated successfully");

        List<String> entriesToBeModified = new ArrayList<>();
        for (String key : requestBody.keySet()) {
            if (key.equals(configMap.get(SMSConstants.SENDER_PASSWORD_IDENTIFIER))) {
                entriesToBeModified.add(key);
            } else if (key.equals(configMap.get(SMSConstants.SENDER_SECUREKEY_IDENTIFIER))) {
                entriesToBeModified.add(key);
            }
        }
        log.debug("Found {} entries to be modified", entriesToBeModified.size());
        
        if (!CollectionUtils.isEmpty(entriesToBeModified)) {
            for (String key : entriesToBeModified) {
                if (key.equals(configMap.get(SMSConstants.SENDER_PASSWORD_IDENTIFIER))) {
                    requestBody.remove(key);
                    requestBody.add(key, encryptedPwd);
                    log.debug("Password replaced with encrypted value");
                } else if (key.equals(configMap.get(SMSConstants.SENDER_SECUREKEY_IDENTIFIER))) {
                    requestBody.remove(key);
                    requestBody.add(key, hashMsg);
                    log.debug("Secure key replaced with hash message");
                }
            }
        }
        log.debug("Post-processing completed");
    }

    /**
     * A map to fetch the configured keys for attributes.
     *
     * @return
     */
    public Map<String, String> getConfigMap() {
        log.trace("getConfigMap method invoked");
        Map<String, String> configMap = new HashMap<>();
        int processedCount = 0;
        for (String key : smsProperties.getConfigMap().keySet()) {
            String value = smsProperties.getConfigMap().get(key);
            if (value.contains("$")) {
                if (value.equals("$username"))
                    configMap.put(SMSConstants.SENDER_USERNAME_IDENTIFIER, key);
                else if (value.equals("$password"))
                    configMap.put(SMSConstants.SENDER_PASSWORD_IDENTIFIER, key);
                else if (value.equals("$senderid"))
                    configMap.put(SMSConstants.SENDER_SENDERID_IDENTIFIER, key);
                else if (value.equals("$securekey"))
                    configMap.put(SMSConstants.SENDER_SECUREKEY_IDENTIFIER, key);
                else if (value.equals("$mobileno"))
                    configMap.put(SMSConstants.SENDER_MOBNO_IDENTIFIER, key);
                else if (value.equals("$message"))
                    configMap.put(SMSConstants.SENDER_MESSAGE_IDENTIFIER, key);
                processedCount++;
            }
        }
        log.debug("Config map built with {} entries from {} processed values", configMap.size(), processedCount);
        return configMap;
    }

    /**
     * Hash generator
     *
     * @param userName
     * @param senderId
     * @param content
     * @param secureKey
     * @return
     */
    private String hashGenerator(String userName, String senderId, String content, String secureKey) {
        log.trace("hashGenerator method invoked");
        StringBuffer finalString = new StringBuffer();
        finalString.append(userName.trim()).append(senderId.trim()).append(content.trim()).append(secureKey.trim());
        String hashGen = finalString.toString();
        log.debug("Hash input string constructed, length: {}", hashGen.length());
        
        StringBuffer sb = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(hashGen.getBytes());
            byte byteData[] = md.digest();
            log.debug("SHA-512 digest generated, byte array length: {}", byteData.length);
            
            // convert the byte to hex format method 1
            sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            log.debug("Hash converted to hex format, result length: {}", sb.length());

        } catch (Exception e) {
            log.error("Exception occurred while generating hash", e);
        }
        return sb != null ? sb.toString() : null;
    }

}
