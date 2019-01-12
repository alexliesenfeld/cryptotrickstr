package com.schumskich.cryptotrickstr.support.cryptocompare.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TopCoinsResponse extends AbstractCryptoCompareResponse {

    @JsonProperty("Data")
    private List<ResponseData> responseData;

    @Getter
    @Setter
    public static class ResponseData {
        @JsonProperty("CoinInfo")
        private CoinInfo coinInfo;
    }

    @Getter
    @Setter
    public static class CoinInfo {
        @JsonProperty("Name")
        private String name;
    }
}
