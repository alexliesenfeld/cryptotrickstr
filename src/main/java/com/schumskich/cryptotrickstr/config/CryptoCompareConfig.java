package com.schumskich.cryptotrickstr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cryptocompare")
public class CryptoCompareConfig {
    @NotNull
    String url;

    @NotNull
    List<String> exchangeWhitelist;

    @NotNull
    Integer exchangeDataStaleAfter;

    @NotNull
    String topSymbolsByVolumePath;

    @NotNull
    String symbolSnapshotPath;
}
