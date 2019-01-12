package com.schumskich.cryptotrickstr.tasks;

import com.schumskich.cryptotrickstr.app.coins.persistence.CoinEntity;
import com.schumskich.cryptotrickstr.app.coins.persistence.CoinRepository;
import com.schumskich.cryptotrickstr.app.coins.tasks.InitializeDatabaseTask;
import com.schumskich.cryptotrickstr.config.CryptoCompareConfig;
import com.schumskich.cryptotrickstr.config.OrikaBeanMapper;
import com.schumskich.cryptotrickstr.support.cryptocompare.CryptoCompareAdapter;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.CoinSnapshotResponse;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.TopCoinsResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("initializeDatabaseTaskTest")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitializeDatabaseTaskTest {

    @Autowired
    private CryptoCompareConfig cryptoCompareConfig;

    @Autowired
    private OrikaBeanMapper mapper;

    @MockBean
    private CryptoCompareAdapter adapterMock;

    @MockBean
    private CoinRepository repositoryMock;

    @After
    public void tearDown() {
        this.cryptoCompareConfig.getExchangeWhitelist().remove("Exchange0");
        this.cryptoCompareConfig.getExchangeWhitelist().remove("Exchange1");
    }

    @Test
    public void doesNotStartIfParameterNotSet() {
        // Run the code under test
        new InitializeDatabaseTask(false, cryptoCompareConfig, adapterMock, mapper, repositoryMock)
                .onApplicationEvent(null);

        // Check test result
        verify(adapterMock, times(0)).fetchTopCoins();
    }

    @Test
    public void usesAdapter() {
        // Set up mocks
        Mockito.when(adapterMock.fetchTopCoins()).thenReturn(createDefaultTopCoinsResponse());
        Mockito.when(adapterMock.fetchCoinSnapshot(Matchers.matches("BTC")))
                .thenReturn(createDefaultCoinSnapshotResponse("BTC"));

        // Run the code under test
        new InitializeDatabaseTask(true, cryptoCompareConfig, adapterMock, mapper, repositoryMock)
                .onApplicationEvent(null);

        // Check test result
        verify(adapterMock, times(1)).fetchTopCoins();
        verify(adapterMock, times(1)).fetchCoinSnapshot("BTC");
    }

    @Test
    public void successfullyStoresNewCoinEntity() {
        // Set up exchange whitelist
        cryptoCompareConfig.getExchangeWhitelist().add("Exchange0");
        cryptoCompareConfig.getExchangeWhitelist().add("Exchange1");

        // Set up mocks
        Mockito.when(adapterMock.fetchTopCoins()).thenReturn(createDefaultTopCoinsResponse());
        Mockito.when(adapterMock.fetchCoinSnapshot(Matchers.matches("BTC")))
                .thenReturn(createDefaultCoinSnapshotResponse("BTC"));
        Mockito.when(repositoryMock.findOneBySymbol(any(String.class))).thenReturn(null);

        // Run the code under test
        Date testStartTime = new Date();
        new InitializeDatabaseTask(true, cryptoCompareConfig, adapterMock, mapper, repositoryMock)
                .onApplicationEvent(null);

        // Check test result
        ArgumentCaptor<CoinEntity> captor = ArgumentCaptor.forClass(CoinEntity.class);
        Mockito.verify(repositoryMock).save(captor.capture());
        verifyDefaultCoinEntity(captor, testStartTime);
    }

    @Test
    public void successfullyUpdatesExistingCoinEntity() {
        // Set up exchange whitelist
        cryptoCompareConfig.getExchangeWhitelist().add("Exchange0");
        cryptoCompareConfig.getExchangeWhitelist().add("Exchange1");

        // Set up mocks
        Mockito.when(adapterMock.fetchTopCoins()).thenReturn(createDefaultTopCoinsResponse());
        Mockito.when(adapterMock.fetchCoinSnapshot(Matchers.matches("BTC")))
                .thenReturn(createDefaultCoinSnapshotResponse("BTC"));
        // Returning an "existing" but empty object to update
        Mockito.when(repositoryMock.findOneBySymbol(any(String.class))).thenReturn(new CoinEntity());

        // Run the code under test
        Date testStartTime = new Date();
        new InitializeDatabaseTask(true, cryptoCompareConfig, adapterMock, mapper, repositoryMock)
                .onApplicationEvent(null);

        // Check test result
        ArgumentCaptor<CoinEntity> captor = ArgumentCaptor.forClass(CoinEntity.class);
        Mockito.verify(repositoryMock).save(captor.capture());
        verifyDefaultCoinEntity(captor, testStartTime);
    }

    @Test
    public void doesNotUpdatePricesIfExchangeIsNotWhitelisted() {
        // Set up mocks
        Mockito.when(adapterMock.fetchTopCoins()).thenReturn(createDefaultTopCoinsResponse());
        Mockito.when(adapterMock.fetchCoinSnapshot(Matchers.matches("BTC")))
                .thenReturn(createDefaultCoinSnapshotResponse("BTC"));
        // Returning an "existing" but empty object to update
        Mockito.when(repositoryMock.findOneBySymbol(any(String.class))).thenReturn(new CoinEntity());

        // Run the code under test
        new InitializeDatabaseTask(true, cryptoCompareConfig, adapterMock, mapper, repositoryMock)
                .onApplicationEvent(null);

        // Check test result
        ArgumentCaptor<CoinEntity> captor = ArgumentCaptor.forClass(CoinEntity.class);
        Mockito.verify(repositoryMock).save(captor.capture());

        // Verify price information
        assertThat(captor.getValue().getMinPrice(), is(equalTo(null)));
        assertThat(captor.getValue().getMinPriceExchange(), is(equalTo(null)));
        assertThat(captor.getValue().getMinPriceUpdated(), is(equalTo(null)));
        assertThat(captor.getValue().getMaxPrice(), is(equalTo(null)));
        assertThat(captor.getValue().getMaxPriceExchange(), is(equalTo(null)));
        assertThat(captor.getValue().getMaxPriceUpdated(), is(equalTo(null)));
        assertThat(captor.getValue().getPriceGap(), is(equalTo(null)));
        assertThat(captor.getValue().getPriceGapPercent(), is(equalTo(null)));
    }

    @Test
    public void doesNotSaveOnUnprocessableEntity() {
        // Set up exchange whitelist
        cryptoCompareConfig.getExchangeWhitelist().add("Exchange0");
        cryptoCompareConfig.getExchangeWhitelist().add("Exchange1");

        // Set up mocks
        Mockito.when(adapterMock.fetchTopCoins()).thenReturn(createDefaultTopCoinsResponse());
        Mockito.when(adapterMock.fetchCoinSnapshot(Matchers.matches("BTC")))
                .thenReturn(new CoinSnapshotResponse()); // Entity is empty (null values only)
        Mockito.when(repositoryMock.findOneBySymbol(any(String.class))).thenReturn(null);

        // Run the code under test
        new InitializeDatabaseTask(true, cryptoCompareConfig, adapterMock, mapper, repositoryMock)
                .onApplicationEvent(null);

        // Check test result
        verify(repositoryMock, times(0)).save(Matchers.any(CoinEntity.class));
    }

    private void verifyDefaultCoinEntity(ArgumentCaptor<CoinEntity> captor, Date testStartTime) {
        // Verify mapping
        assertThat(captor.getValue().getSymbol(), is(equalTo("BTC")));
        assertThat(captor.getValue().getFullName(), is(equalTo("BTCCoin")));
        assertThat(captor.getValue().getTotalCoinsMined(), is(equalTo(5L)));
        assertThat(captor.getValue().getAggregatedPrice(), is(equalTo(100.5)));
        assertThat(captor.getValue().getAggregatedDayPriceOpen(), is(equalTo(600.2)));
        assertThat(captor.getValue().getTotalVolume24H(), is(equalTo(500000d)));

        // Verify price information
        assertThat(captor.getValue().getMinPrice(), is(equalTo(1100.4)));
        assertThat(captor.getValue().getMinPriceExchange(), is(equalTo("Exchange0")));
        assertThat(captor.getValue().getMinPriceUpdated(), is(greaterThan(testStartTime)));
        assertThat(captor.getValue().getMaxPrice(), is(equalTo(2100.4)));
        assertThat(captor.getValue().getMaxPriceExchange(), is(equalTo("Exchange1")));
        assertThat(captor.getValue().getMaxPriceUpdated(), is(greaterThan(testStartTime)));
        assertThat(captor.getValue().getPriceGap(), is(equalTo(1000.0)));
        assertThat(captor.getValue().getPriceGapPercent(), is(equalTo(90.87604507451836)));

        // Verify class does not touch the date of creation / update (set automatically by JPA)
        assertThat(captor.getValue().getDateCreated(), is(equalTo(null)));
        assertThat(captor.getValue().getDateUpdated(), is(equalTo(null)));
    }

    private TopCoinsResponse createDefaultTopCoinsResponse() {
        return new TopCoinsResponse() {{
            setResponse("Success");
            setMessage("OK");
            setResponseData(new LinkedList<TopCoinsResponse.ResponseData>() {{
                add(new TopCoinsResponse.ResponseData() {{
                    setCoinInfo(new CoinInfo() {{
                        setName("BTC");
                    }});
                }});
            }});
        }};
    }

    private CoinSnapshotResponse createDefaultCoinSnapshotResponse(String symbol) {
        return new CoinSnapshotResponse() {{
            setResponse("Success");
            setMessage("OK");
            setResponseData(new ResponseData() {{
                setAggregatedData(new AggregatedData() {{
                    setPrice(100.5);
                    setLastUpdated(new Date());
                    setOpenDay(600.2);
                    setHighDay(700.8);
                    setLowDay(100d);
                }});
                setCoinInfo(new CoinInfo() {{
                    setName(symbol);
                    setFullName(symbol + "Coin");
                    setTotalCoinsMined(5L);
                    setTotalVolume24H(500000.0);
                }});
                setExchangeInfoList(new LinkedList<ExchangeInfo>() {{
                    add(new ExchangeInfo() {{
                        setExchangeName("Exchange" + size());
                        setPrice(1100.4);
                        setLastUpdated(Date.from(Instant.now().plus(2, ChronoUnit.MINUTES)));
                        setOpen24H(1200d);
                        setHigh24H(1400d);
                        setLow24H(150.5);
                        setVolume24H(15000d);
                    }});
                    add(new ExchangeInfo() {{
                        setExchangeName("Exchange" + size());
                        setPrice(2100.4);
                        setLastUpdated(Date.from(Instant.now().plus(3, ChronoUnit.MINUTES)));
                        setOpen24H(2200d);
                        setHigh24H(2400d);
                        setLow24H(250.5);
                        setVolume24H(25000d);
                    }});
                }});
            }});
        }};
    }

}