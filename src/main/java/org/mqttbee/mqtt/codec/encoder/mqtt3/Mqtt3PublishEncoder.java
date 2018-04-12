package org.mqttbee.mqtt.codec.encoder.mqtt3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5PubAckEncoder;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5PubRecEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPublishEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider.ThreadLocalMqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class Mqtt3PublishEncoder extends Mqtt3WrappedMessageEncoder<MqttPublish, MqttPublishWrapper> {

    private static final MqttPublishEncoderProvider WRAPPER_PROVIDER =
            new MqttPublishEncoderProvider(Mqtt5PubAckEncoder.PROVIDER, Mqtt5PubRecEncoder.PROVIDER);
    public static final MqttWrappedMessageEncoderProvider<MqttPublish, MqttPublishWrapper, MqttPublishEncoderProvider>
            PROVIDER = new ThreadLocalMqttWrappedMessageEncoderProvider<>(Mqtt3PublishEncoder::new, WRAPPER_PROVIDER);

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBLISH.getCode() << 4;

    @Override
    int calculateRemainingLength() {
        int remainingLength = 0;

        remainingLength += wrapped.getTopic().encodedLength();

        if (wrapped.getQos() != MqttQoS.AT_MOST_ONCE) {
            remainingLength += 2;
        }

        final ByteBuffer payload = wrapped.getRawPayload();
        if (payload != null) {
            remainingLength += payload.remaining();
        }

        return remainingLength;
    }

    @Override
    public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
        encodeFixedHeader(out);
        encodeVariableHeader(out);
        encodePayload(out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        int flags = 0;
        if (message.isDup()) {
            flags |= 0b1000;
        }
        flags |= wrapped.getQos().getCode() << 1;
        if (wrapped.isRetain()) {
            flags |= 0b0001;
        }

        out.writeByte(FIXED_HEADER | flags);

        MqttVariableByteInteger.encode(remainingLength(), out);
    }

    private void encodeVariableHeader(@NotNull final ByteBuf out) {
        wrapped.getTopic().to(out);

        if (wrapped.getQos() != MqttQoS.AT_MOST_ONCE) {
            out.writeShort(message.getPacketIdentifier());
        }
    }

    private void encodePayload(@NotNull final ByteBuf out) {
        final ByteBuffer payload = wrapped.getRawPayload();
        if (payload != null) {
            out.writeBytes(payload.duplicate());
        }
    }

}
