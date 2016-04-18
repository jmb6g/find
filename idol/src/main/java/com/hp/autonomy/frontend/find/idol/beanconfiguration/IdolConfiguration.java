/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.autonomy.aci.client.annotations.IdolAnnotationsProcessorFactory;
import com.autonomy.aci.client.services.AciService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.hp.autonomy.frontend.configuration.*;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsDeserializer;
import com.hp.autonomy.frontend.find.idol.configuration.IdolAuthenticationMixins;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfigFileService;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import com.hp.autonomy.user.UserService;
import com.hp.autonomy.user.UserServiceImpl;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@ImportResource("required-statistics.xml")
public class IdolConfiguration {
    @Autowired
    private TextEncryptor textEncryptor;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Autowired
    public ObjectMapper jacksonObjectMapper(final Jackson2ObjectMapperBuilder builder, final QueryRestrictionsDeserializer<?> queryRestrictionsDeserializer) {
        return builder
                .createXmlMapper(false)
                .mixIn(Authentication.class, IdolAuthenticationMixins.class)
                .deserializerByType(QueryRestrictions.class, queryRestrictionsDeserializer)
                .build();
    }


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Autowired
    public IdolFindConfigFileService configFileService(final Jackson2ObjectMapperBuilder builder, final FilterProvider filterProvider) {
        final ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.addMixIn(Authentication.class, IdolAuthenticationMixins.class);
        objectMapper.addMixIn(ServerConfig.class, ConfigurationFilterMixin.class);
        objectMapper.addMixIn(ViewConfig.class, ConfigurationFilterMixin.class);
        objectMapper.addMixIn(IdolFindConfig.class, ConfigurationFilterMixin.class);

        final IdolFindConfigFileService configService = new IdolFindConfigFileService();
        configService.setConfigFileLocation("hp.find.home");
        configService.setConfigFileName("config.json");
        configService.setDefaultConfigFile("/defaultIdolConfigFile.json");
        configService.setMapper(objectMapper);
        configService.setTextEncryptor(textEncryptor);
        configService.setFilterProvider(filterProvider);

        return configService;
    }

    @Bean
    public UserService userService(final ConfigService<IdolFindConfig> configService, final AciService aciService, final AciResponseJaxbProcessorFactory processorFactory) {
        return new UserServiceImpl(configService, aciService, processorFactory);
    }

    @Bean
    @Autowired
    public CommunityService communityService(final AciService aciService, final IdolAnnotationsProcessorFactory idolAnnotationsProcessorFactory) {
        final CommunityServiceImpl communityService = new CommunityServiceImpl();
        communityService.setAciService(aciService);
        communityService.setProcessorFactory(idolAnnotationsProcessorFactory);

        return communityService;
    }

    @Bean
    public CommunityAuthenticationValidator communityAuthenticationValidator(
            final AciService validatorAciService,
            final IdolAnnotationsProcessorFactory processorFactory
    ) {
        final CommunityAuthenticationValidator communityAuthenticationValidator = new CommunityAuthenticationValidator();

        communityAuthenticationValidator.setAciService(validatorAciService);
        communityAuthenticationValidator.setProcessorFactory(processorFactory);

        return communityAuthenticationValidator;
    }

    @Bean
    public ServerConfigValidator serverConfigValidator(
            final AciService validatorAciService,
            final IdolAnnotationsProcessorFactory processorFactory
    ) {
        final ServerConfigValidator serverConfigValidator = new ServerConfigValidator();

        serverConfigValidator.setAciService(validatorAciService);
        serverConfigValidator.setProcessorFactory(processorFactory);

        return serverConfigValidator;
    }
}
