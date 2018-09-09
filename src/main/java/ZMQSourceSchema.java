import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class ZMQSourceSchema implements DeserializationSchema<Trade>, SerializationSchema<Trade> {

    private static final long serialVersionUID = 444L;

    public byte[] serialize(Trade trade) {
        return trade.toByteArray();
    }

    public Trade deserialize(byte[] message) throws IOException {
        return Trade.parseFrom(message);
    }

    public boolean isEndOfStream(Trade nextTrade) {
        return false;
    }

    public TypeInformation<Trade> getProducedType() {
        return TypeInformation.of(Trade.class);
    }
}
