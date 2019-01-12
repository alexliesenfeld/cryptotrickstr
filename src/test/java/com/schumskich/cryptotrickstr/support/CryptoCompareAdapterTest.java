package com.schumskich.cryptotrickstr.support;

import com.schumskich.cryptotrickstr.config.CryptoCompareConfig;
import com.schumskich.cryptotrickstr.support.cryptocompare.CryptoCompareAdapterImpl;
import com.schumskich.cryptotrickstr.support.cryptocompare.exceptions.CryptoCompareApiException;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.CoinSnapshotResponse;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.TopCoinsResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("initializeDatabaseTaskTest")
@RunWith(SpringRunner.class)
@SpringBootTest
public class CryptoCompareAdapterTest {

    @Autowired
    private CryptoCompareConfig cryptoCompareConfig;

    @MockBean
    private RestTemplate restTemplateMock;

    @Test
    public void doesNotThrowExceptionsOnSuccessfulResponse() {
        // Set up mocks
        Mockito.when(restTemplateMock.getForEntity(Matchers.anyString(), Matchers.anyObject()))
                .thenReturn(new ResponseEntity<>(new CoinSnapshotResponse(), HttpStatus.OK));

        // Run the code under test
        new CryptoCompareAdapterImpl(cryptoCompareConfig, restTemplateMock).fetchCoinSnapshot("");
    }

    @Test(expected = CryptoCompareApiException.class)
    public void throwsExceptionIfStatusCodeIsNot2xx() {
        // Set up mocks
        Mockito.when(restTemplateMock.getForEntity(Matchers.anyString(), Matchers.anyObject()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Run the code under test
        new CryptoCompareAdapterImpl(cryptoCompareConfig, restTemplateMock).fetchCoinSnapshot("");
    }

    @Test(expected = CryptoCompareApiException.class)
    public void throwsExceptionIfOnErrorMessage() {
        // Set up mocks
        Mockito.when(restTemplateMock.getForEntity(Matchers.anyString(), Matchers.anyObject()))
                .thenReturn(new ResponseEntity<>(new TopCoinsResponse() {{
                    setMessage("Error");
                }}, HttpStatus.OK));

        // Run the code under test
        new CryptoCompareAdapterImpl(cryptoCompareConfig, restTemplateMock).fetchCoinSnapshot("");
    }

    @Test(expected = CryptoCompareApiException.class)
    public void throwsExceptionIfOnErrorResponse() {
        // Set up mocks
        Mockito.when(restTemplateMock.getForEntity(Matchers.anyString(), Matchers.anyObject()))
                .thenReturn(new ResponseEntity<>(new TopCoinsResponse() {{
                    setResponse("Error");
                }}, HttpStatus.OK));

        // Run the code under test
        new CryptoCompareAdapterImpl(cryptoCompareConfig, restTemplateMock).fetchCoinSnapshot("");
    }
}