package com.schumskich.cryptotrickstr.app.coins.persistence;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "coins", path = "/coins")
public interface CoinRepository extends PagingAndSortingRepository<CoinEntity, Long> {
    CoinEntity findOneBySymbol(@Param("symbol") String symbol);

    List<CoinEntity> findByFullName(@Param("name") String name);

    List<CoinEntity> findByPriceGapBetween(@Param("from") Double from, @Param("to") Double to);

    List<CoinEntity> findByPriceGapPercentBetween(@Param("from") Double from, @Param("to") Double to);
}