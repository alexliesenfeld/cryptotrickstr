package com.schumskich.cryptotrickstr.support.cryptocompare.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractCryptoCompareResponse {

    @JsonProperty("Response")
    private String response;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("ErrorsSummary")
    private String errorSummary;

}
