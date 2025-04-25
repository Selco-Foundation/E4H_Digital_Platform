package com.example.hfr.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * ResponseInfo should be used to carry metadata information about the response from the server. apiId, ver and msgId in ResponseInfo should always correspond to the same values in respective request&#39;s RequestInfo.
 */

@Schema(name = "FacilityCreateResponse_ResponseInfo", description = "ResponseInfo should be used to carry metadata information about the response from the server. apiId, ver and msgId in ResponseInfo should always correspond to the same values in respective request's RequestInfo.")
@JsonTypeName("FacilityCreateResponse_ResponseInfo")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityCreateResponseResponseInfo {

    private String apiId;

    private String ver;

    private Long ts;

    private String resMsgId;

    private String msgId;
    private StatusEnum status;

    public FacilityCreateResponseResponseInfo() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public FacilityCreateResponseResponseInfo(String apiId, String ver, Long ts, StatusEnum status) {
        this.apiId = apiId;
        this.ver = ver;
        this.ts = ts;
        this.status = status;
    }

    public FacilityCreateResponseResponseInfo apiId(String apiId) {
        this.apiId = apiId;
        return this;
    }

    /**
     * unique API ID
     *
     * @return apiId
     */
    @NotNull
    @Size(max = 128)
    @Schema(name = "apiId", description = "unique API ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("apiId")
    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public FacilityCreateResponseResponseInfo ver(String ver) {
        this.ver = ver;
        return this;
    }

    /**
     * API version
     *
     * @return ver
     */
    @NotNull
    @Size(max = 32)
    @Schema(name = "ver", description = "API version", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("ver")
    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public FacilityCreateResponseResponseInfo ts(Long ts) {
        this.ts = ts;
        return this;
    }

    /**
     * response time in epoch
     *
     * @return ts
     */
    @NotNull
    @Schema(name = "ts", description = "response time in epoch", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("ts")
    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public FacilityCreateResponseResponseInfo resMsgId(String resMsgId) {
        this.resMsgId = resMsgId;
        return this;
    }

    /**
     * unique response message id (UUID) - will usually be the correlation id from the server
     *
     * @return resMsgId
     */
    @Size(max = 256)
    @Schema(name = "resMsgId", description = "unique response message id (UUID) - will usually be the correlation id from the server", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("resMsgId")
    public String getResMsgId() {
        return resMsgId;
    }

    public void setResMsgId(String resMsgId) {
        this.resMsgId = resMsgId;
    }

    public FacilityCreateResponseResponseInfo msgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    /**
     * message id of the request
     *
     * @return msgId
     */
    @Size(max = 256)
    @Schema(name = "msgId", description = "message id of the request", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("msgId")
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public FacilityCreateResponseResponseInfo status(StatusEnum status) {
        this.status = status;
        return this;
    }

    /**
     * status of request processing - to be enhanced in futuer to include INPROGRESS
     *
     * @return status
     */
    @NotNull
    @Schema(name = "status", description = "status of request processing - to be enhanced in futuer to include INPROGRESS", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("status")
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityCreateResponseResponseInfo facilityCreateResponseResponseInfo = (FacilityCreateResponseResponseInfo) o;
        return Objects.equals(this.apiId, facilityCreateResponseResponseInfo.apiId) &&
                Objects.equals(this.ver, facilityCreateResponseResponseInfo.ver) &&
                Objects.equals(this.ts, facilityCreateResponseResponseInfo.ts) &&
                Objects.equals(this.resMsgId, facilityCreateResponseResponseInfo.resMsgId) &&
                Objects.equals(this.msgId, facilityCreateResponseResponseInfo.msgId) &&
                Objects.equals(this.status, facilityCreateResponseResponseInfo.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiId, ver, ts, resMsgId, msgId, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityCreateResponseResponseInfo {\n");
        sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
        sb.append("    ver: ").append(toIndentedString(ver)).append("\n");
        sb.append("    ts: ").append(toIndentedString(ts)).append("\n");
        sb.append("    resMsgId: ").append(toIndentedString(resMsgId)).append("\n");
        sb.append("    msgId: ").append(toIndentedString(msgId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * status of request processing - to be enhanced in futuer to include INPROGRESS
     */
    public enum StatusEnum {
        SUCCESSFUL("SUCCESSFUL"),

        FAILED("FAILED");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static StatusEnum fromValue(String value) {
            for (StatusEnum b : StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

