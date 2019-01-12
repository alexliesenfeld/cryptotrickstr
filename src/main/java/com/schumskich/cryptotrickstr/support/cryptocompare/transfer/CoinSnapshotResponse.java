package com.schumskich.cryptotrickstr.support.cryptocompare.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CoinSnapshotResponse extends AbstractCryptoCompareResponse {

    @JsonProperty("Data")
    private ResponseData responseData;

    @Getter
    @Setter
    public static class ResponseData {
        @JsonProperty("CoinInfo")
        private CoinInfo coinInfo;

        @JsonProperty("AggregatedData")
        private AggregatedData aggregatedData;

        @JsonProperty("Exchanges")
        private List<ExchangeInfo> exchangeInfoList;
    }

    @Getter
    @Setter
    public static class AggregatedData {

        @JsonProperty("PRICE")
        private Double price;

        @JsonProperty("LASTUPDATE")
        private Date lastUpdated;

        @JsonProperty("OPENDAY")
        private Double openDay;

        @JsonProperty("HIGHDAY")
        private Double highDay;

        @JsonProperty("LOWDAY")
        private Double lowDay;

    }

    @Getter
    @Setter
    public static class CoinInfo {

        @JsonProperty("Name")
        private String name;

        @JsonProperty("FullName")
        private String fullName;

        @JsonProperty("TotalCoinsMined")
        private Long totalCoinsMined;

        @JsonProperty("TotalVolume24H")
        private Double totalVolume24H;
    }

    @Getter
    @Setter
    public static class ExchangeInfo {

        @JsonProperty("MARKET")
        private String exchangeName;

        @JsonProperty("PRICE")
        private Double price;

        @JsonProperty("LASTUPDATE")
        @JsonDeserialize(using = UnixTimestampDeserializer.class)
        private Date lastUpdated;

        @JsonProperty("OPEN24HOUR")
        private Double open24H;

        @JsonProperty("HIGH24HOUR")
        private Double high24H;

        @JsonProperty("LOW24HOUR")
        private Double low24H;

        @JsonProperty("VOLUME24HOUR")
        private Double volume24H;

    }
}
