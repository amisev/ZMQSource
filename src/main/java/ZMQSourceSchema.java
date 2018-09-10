import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple5;
import scala.Tuple6;
import sources.TradeOuterClass;

import java.io.IOException;

public class ZMQSourceSchema implements
        DeserializationSchema<Trade>,
                SerializationSchema<Trade> {

    private static final long serialVersionUID = 444L;

    public byte[] serialize(Trade trade) {
        TradeOuterClass.Trade trade_ = TradeOuterClass.Trade.newBuilder()
                .setTradeId(trade.getTradeId())
                .setPrice(trade.getPrice())
                .setQty(trade.getQty())
                .setEventTime(trade.getEventTime())
                .setIngestionTime(trade.getIngestionTime())
                .build();

        return trade_.toByteArray();
    }

    public Trade deserialize(byte[] message) throws IOException {

        TradeOuterClass.Trade trade_ = TradeOuterClass.Trade.parseFrom(message);
        return new Trade(trade_.getTradeId(),
                trade_.getPrice(),
                trade_.getQty(),
                trade_.getEventTime(),
                trade_.getIngestionTime());
    }

    public boolean isEndOfStream(Trade nextTrade) {
        return false;
    }

    public TypeInformation<Trade> getProducedType() {
        return TypeInformation.of(Trade.class);
    }
}
