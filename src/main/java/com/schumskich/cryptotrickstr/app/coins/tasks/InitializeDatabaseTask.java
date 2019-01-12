package com.schumskich.cryptotrickstr.app.coins.tasks;

import com.schumskich.cryptotrickstr.app.coins.persistence.CoinEntity;
import com.schumskich.cryptotrickstr.app.coins.persistence.CoinRepository;
import com.schumskich.cryptotrickstr.config.CryptoCompareConfig;
import com.schumskich.cryptotrickstr.config.OrikaBeanMapper;
import com.schumskich.cryptotrickstr.support.cryptocompare.CryptoCompareAdapter;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.CoinSnapshotResponse;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.TopCoinsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class InitializeDatabaseTask implements ApplicationListener<ApplicationReadyEvent> {
    private static Logger LOG = LoggerFactory.getLogger(InitializeDatabaseTask.class);

    private final CryptoCompareAdapter cryptoCompareAdapter;
    private final OrikaBeanMapper mapper;
    private final CoinRepository coinRepository;
    private final CryptoCompareConfig config;
    private final boolean initDb;

    @Autowired
    public InitializeDatabaseTask(
            @Value("${initDb:false}") boolean initDb,
            CryptoCompareConfig cryptoCompareConfig,
            CryptoCompareAdapter cryptoCompareAdapter,
            OrikaBeanMapper beanMapper,
            CoinRepository coinRepository
    ) {
        this.cryptoCompareAdapter = cryptoCompareAdapter;
        this.mapper = beanMapper;
        this.coinRepository = coinRepository;
        this.config = cryptoCompareConfig;
        this.initDb = initDb;
    }

    /**
     * This method fetches current coin information from a remote API and saves/updates it in the database.
     *
     * @param event The {@link ApplicationReadyEvent} passed by Spring.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (!this.initDb) {
            LOG.info("Won't initialize the database because ‘initDb' parameter was not provided.");
            return;
        }

        LOG.info("Initializing the database with coin data from remote API ...");

        List<String> symbols = this.cryptoCompareAdapter.fetchTopCoins().getResponseData().stream()
                .map(TopCoinsResponse.ResponseData::getCoinInfo)
                .map(TopCoinsResponse.CoinInfo::getName)
                .collect(Collectors.toList());

        int coinsStored = 0;
        int coinsProcessed = 0;
        for (String symbol : symbols) {
            try {
                CoinSnapshotResponse coinSnapshotResponse = this.cryptoCompareAdapter.fetchCoinSnapshot(symbol);
                boolean success = processResponse(symbol, coinSnapshotResponse);
                if (success) {
                    coinsStored++;
                }
                coinsProcessed++;
            } catch (Throwable t) {
                LOG.error("Could not fetch a snapshot of symbol '" + symbol + "'.", t);
            }
        }

        LOG.info("Finished initializing the database (stored " + coinsStored + "/" + coinsProcessed + " coins). Have fun!");
    }

    /**
     * Processes a {@link CoinSnapshotResponse}.
     *
     * @param symbol   The cryptocurrency symbol for which to process the request.
     * @param response The response object to process.
     * @return Whether the response has been successfully processed.
     */
    private boolean processResponse(String symbol, CoinSnapshotResponse response) {
        if (isUnprocessableCoinSnapshotResponse(response)) {
            LOG.debug("Cannot process response, because a required field is not present.");
            return false;
        }

        CoinEntity entity = this.coinRepository.findOneBySymbol(symbol);
        if (entity == null) {
            entity = new CoinEntity();
        }

        mapToEntity(response, entity);
        setPrices(filterExchangesFromResponse(response), entity);

        this.coinRepository.save(entity);
        return true;
    }

    /**
     * Finds a {@link CoinSnapshotResponse.ExchangeInfo} object with a minimum and maximum price and sets all
     * related fields in the given {@link CoinEntity} object.
     *
     * @param exchangeInfo List with price information.
     * @param entity       The entity to set the price information.
     */
    private void setPrices(List<CoinSnapshotResponse.ExchangeInfo> exchangeInfo, CoinEntity entity) {
        CoinSnapshotResponse.ExchangeInfo minPriceExchangeInfo = exchangeInfo.stream()
                .min(Comparator.comparing(CoinSnapshotResponse.ExchangeInfo::getPrice))
                .orElse(null);
        CoinSnapshotResponse.ExchangeInfo maxPriceExchangeInfo = exchangeInfo.stream()
                .max(Comparator.comparing(CoinSnapshotResponse.ExchangeInfo::getPrice))
                .orElse(null);

        entity.setMaxPrice(maxPriceExchangeInfo == null ? null : maxPriceExchangeInfo.getPrice());
        entity.setMaxPriceExchange(maxPriceExchangeInfo == null ? null : maxPriceExchangeInfo.getExchangeName());
        entity.setMaxPriceUpdated(maxPriceExchangeInfo == null ? null : maxPriceExchangeInfo.getLastUpdated());

        entity.setMinPrice(minPriceExchangeInfo == null ? null : minPriceExchangeInfo.getPrice());
        entity.setMinPriceExchange(minPriceExchangeInfo == null ? null : minPriceExchangeInfo.getExchangeName());
        entity.setMinPriceUpdated(minPriceExchangeInfo == null ? null : minPriceExchangeInfo.getLastUpdated());

        if (minPriceExchangeInfo != null && maxPriceExchangeInfo != null) {
            entity.setPriceGap(entity.getMaxPrice() - entity.getMinPrice());
            entity.setPriceGapPercent(entity.getPriceGap() / entity.getMinPrice() * 100);
        } else {
            entity.setPriceGap(null);
            entity.setPriceGapPercent(null);
        }
    }

    /**
     * Filters all {@link CoinSnapshotResponse.ExchangeInfo} objects and removes from all which are not from
     * whitelisted exchanges.
     *
     * @param response The response to filter.
     * @return All information from whitelisted exchanges.
     */
    private List<CoinSnapshotResponse.ExchangeInfo> filterExchangesFromResponse(CoinSnapshotResponse response) {
        long timeNowUtc = Date.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()).getTime();
        long staleExchangeDataPeriod = TimeUnit.HOURS.toMillis(this.config.getExchangeDataStaleAfter());
        return response.getResponseData().getExchangeInfoList().stream()
                .filter(info -> this.config.getExchangeWhitelist().contains(info.getExchangeName()))
                .filter(info -> timeNowUtc - info.getLastUpdated().getTime() < staleExchangeDataPeriod)
                .collect(Collectors.toList());
    }

    /**
     * Maps the values from a {@link CoinSnapshotResponse} object to a {@link CoinEntity} object.
     * <code>minPrice, minPriceExchange, minPriceUpdated and maxPrice, maxPriceExchange, maxPriceUpdated</code>
     * won't be mapped.
     *
     * @param coinSnapshotResponse The response object to map from.
     * @param entity               The entity object to map to.
     */
    private void mapToEntity(CoinSnapshotResponse coinSnapshotResponse, CoinEntity entity) {
        this.mapper.map(coinSnapshotResponse.getResponseData().getCoinInfo(), entity);
        this.mapper.map(coinSnapshotResponse.getResponseData().getAggregatedData(), entity);
    }

    /**
     * Checks if the given {@link CoinSnapshotResponse} object does contain all necessary information to be
     * processed.
     *
     * @param response {@link CoinSnapshotResponse} object to check.
     * @return <code>true</code> if the response can be processed. <code>false</code> otherwise.
     */
    private boolean isUnprocessableCoinSnapshotResponse(CoinSnapshotResponse response) {
        CoinSnapshotResponse.ResponseData data = response.getResponseData();
        return data == null
                || data.getCoinInfo() == null
                || data.getAggregatedData() == null
                || data.getExchangeInfoList() == null;
    }
}
