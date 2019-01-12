package com.schumskich.cryptotrickstr.support.cryptocompare;

import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.CoinSnapshotResponse;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.TopCoinsResponse;

public interface CryptoCompareAdapter {
    TopCoinsResponse fetchTopCoins();

    CoinSnapshotResponse fetchCoinSnapshot(String symbol);
}
