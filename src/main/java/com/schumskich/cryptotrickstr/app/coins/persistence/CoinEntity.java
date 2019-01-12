package com.schumskich.cryptotrickstr.app.coins.persistence;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "IDX_FULL_NAME", columnList = "fullName"),
        @Index(name = "IDX_MIN_PRICE", columnList = "minPrice"),
        @Index(name = "IDX_MAX_PRICE", columnList = "maxPrice")
})
public class CoinEntity {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String symbol;

    @Column
    private String fullName;

    @Column
    private long totalCoinsMined;

    @Column
    private double aggregatedPrice;

    @Column
    private double aggregatedDayPriceOpen;

    @Column
    private double aggregatedDayPriceHigh;

    @Column
    private double aggregatedDayPriceLow;

    @Column
    private double totalVolume24H;

    @Column
    private Double minPrice;

    @Column
    private String minPriceExchange;

    @Column
    private Date minPriceUpdated;

    @Column
    private Double maxPrice;

    @Column
    private String maxPriceExchange;

    @Column
    private Date maxPriceUpdated;

    @Column
    private Double priceGapPercent;

    @Column
    private Double priceGap;

    @Column(nullable = false)
    private Date dateCreated;

    @Column(nullable = false)
    private Date dateUpdated;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        dateUpdated = new Date();
        if (dateCreated == null) {
            dateCreated = new Date();
        }
    }
}