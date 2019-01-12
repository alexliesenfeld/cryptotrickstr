package com.schumskich.cryptotrickstr.support.cryptocompare;

import com.schumskich.cryptotrickstr.config.CryptoCompareConfig;
import com.schumskich.cryptotrickstr.support.cryptocompare.exceptions.CryptoCompareApiException;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.AbstractCryptoCompareResponse;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.CoinSnapshotResponse;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.TopCoinsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CryptoCompareAdapterImpl implements CryptoCompareAdapter {
    private final CryptoCompareConfig config;
    private final RestTemplate restTemplate;

    @Autowired
    public CryptoCompareAdapterImpl(CryptoCompareConfig cryptoCompareConfig, RestTemplate restTemplate) {
        this.config = cryptoCompareConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the top coins by trading volume.
     *
     * @return The response object.
     */
    @Override
    public TopCoinsResponse fetchTopCoins() {
        ResponseEntity<TopCoinsResponse> response =
                restTemplate.getForEntity(config.getTopSymbolsByVolumePath(), TopCoinsResponse.class);
        checkForErrors(response);
        return response.getBody();
    }

    /**
     * Fetches a coin snapshot.
     *
     * @param symbol The symbol of the coin to fetch the data for.
     * @return The response.
     */
    @Override
    public CoinSnapshotResponse fetchCoinSnapshot(String symbol) {
        ResponseEntity<CoinSnapshotResponse> response =
                restTemplate.getForEntity(config.getSymbolSnapshotPath() + symbol, CoinSnapshotResponse.class);
        checkForErrors(response);
        return response.getBody();
    }

    /**
     * This method checks if the response contains any errors.
     *
     * @param response The response to check.
     */
    private void checkForErrors(ResponseEntity<? extends AbstractCryptoCompareResponse> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CryptoCompareApiException(toLogMessageText(response));
        }
        // The remote API does't seem to be polished yet, because we cannot rely on the HTTP status code alone.
        // We also need to check whether the returned message contained "Error" in the "Response" or "Message"
        // field (this is incosistent between requests).
        AbstractCryptoCompareResponse body = response.getBody();
        boolean containsError = "Error".equals(body.getMessage()) || "Error".equals(body.getResponse());
        boolean falsePositive = "No exchanges available".equals(body.getMessage());
        if (containsError && !falsePositive) {
            throw new CryptoCompareApiException(toLogMessageText(response));
        }
    }

    /**
     * This converts a response to a text message.
     *
     * @param response The response to convert.
     * @return The text message.
     */
    private static String toLogMessageText(ResponseEntity<? extends AbstractCryptoCompareResponse> response) {
        AbstractCryptoCompareResponse body = response.getBody();
        String text = "Did not receive a successful response from CryptoCompare. API returned the following values: ";
        text += "HTTP status code = " + response.getStatusCodeValue();
        if (body != null) {
            text += body.getMessage() == null ? "" : ", Message = '" + body.getMessage() + "'";
            text += body.getResponse() == null ? "" : ", Response = '" + body.getResponse() + "'";
            text += body.getErrorSummary() == null ? "" : ", Summary = '" + body.getErrorSummary() + "'";
        }
        return text;
    }
}
