package com.schumskich.cryptotrickstr.config;

import com.schumskich.cryptotrickstr.app.coins.persistence.CoinEntity;
import com.schumskich.cryptotrickstr.support.cryptocompare.transfer.CoinSnapshotResponse;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrikaBeanMapper extends ConfigurableMapper implements ApplicationContextAware {

    private MapperFactory factory;
    private ApplicationContext applicationContext;

    public OrikaBeanMapper() {
        super(false);
    }

    @Override
    protected void configure(final MapperFactory factory) {
        this.factory = factory;
        addAllSpringBeans(applicationContext);

        this.factory.classMap(CoinSnapshotResponse.CoinInfo.class, CoinEntity.class)
                .field("name", "symbol")
                .byDefault()
                .register();

        this.factory.classMap(CoinSnapshotResponse.AggregatedData.class, CoinEntity.class)
                .field("price", "aggregatedPrice")
                .field("openDay", "aggregatedDayPriceOpen")
                .field("highDay", "aggregatedDayPriceHigh")
                .field("lowDay", "aggregatedDayPriceLow")
                .byDefault()
                .register();
    }

    @Override
    protected void configureFactoryBuilder(final DefaultMapperFactory.Builder factoryBuilder) {
        factoryBuilder.mapNulls(false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addMapper(final Mapper<?, ?> mapper) {
        factory.classMap(mapper.getAType(),
                mapper.getBType())
                .byDefault()
                .customize((Mapper) mapper)
                .mapNulls(false)
                .mapNullsInReverse(false)
                .register();
    }

    private void addConverter(final Converter<?, ?> converter) {
        factory.getConverterFactory().registerConverter(converter);
    }

    private void addAllSpringBeans(final ApplicationContext applicationContext) {
        final Map<String, Mapper> mappers = applicationContext.getBeansOfType(Mapper.class);
        for (final Mapper mapper : mappers.values()) {
            addMapper(mapper);
        }
        final Map<String, Converter> converters = applicationContext.getBeansOfType(Converter.class);
        for (final Converter converter : converters.values()) {
            addConverter(converter);
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        init();
    }

}