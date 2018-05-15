/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.api.mqtt.mqtt5;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttSslConfig;
import org.mqttbee.api.mqtt.MqttWebsocketConfig;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt5.Mqtt5ClientImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientBuilder {

    private final MqttClientIdentifierImpl identifier;
    private final String serverHost;
    private final int serverPort;
    private final MqttClientExecutorConfigImpl executorConfig;
    private final MqttWebsocketConfig mqttWebsocketConfig;
    private final MqttSslConfig mqttSslConfig;

    private boolean followsRedirects = false;
    private boolean allowsServerReAuth = false;
    private MqttAdvancedClientData advancedClientData;

    public Mqtt5ClientBuilder(
            @NotNull final MqttClientIdentifierImpl identifier, @NotNull final String serverHost, final int serverPort,
            @Nullable final MqttSslConfig mqttSslConfig, @Nullable final MqttWebsocketConfig mqttWebsocketConfig,
            @NotNull final MqttClientExecutorConfigImpl executorConfig) {

        Preconditions.checkNotNull(identifier);
        Preconditions.checkNotNull(serverHost);

        this.identifier = identifier;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.mqttWebsocketConfig = mqttWebsocketConfig;
        this.mqttSslConfig = mqttSslConfig;
        this.executorConfig = executorConfig;
    }

    @NotNull
    public Mqtt5ClientBuilder followingRedirects(final boolean followsRedirects) {
        this.followsRedirects = followsRedirects;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder allowingServerReAuth(final boolean allowsServerReAuth) {
        this.allowsServerReAuth = allowsServerReAuth;
        return this;
    }

    @NotNull
    public Mqtt5ClientBuilder withAdvanced(@Nullable final Mqtt5AdvancedClientData advancedClientData) {
        this.advancedClientData =
                MustNotBeImplementedUtil.checkNullOrNotImplemented(advancedClientData, MqttAdvancedClientData.class);
        return this;
    }

    @NotNull
    public Mqtt5Client reactive() {
        return new Mqtt5ClientImpl(buildClientData());
    }

    private MqttClientData buildClientData() {
        return new MqttClientData(MqttVersion.MQTT_5_0, identifier, serverHost, serverPort, mqttWebsocketConfig, mqttSslConfig, followsRedirects,
                allowsServerReAuth, executorConfig, advancedClientData);
    }

}
