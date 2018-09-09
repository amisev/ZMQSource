import com.google.protobuf.InvalidProtocolBufferException;
import sources.TradeOuterClass;

public class Trade {
    private long tradeId;
    private double price;
    private double qty;
    private double eventTime;
    private double ingestionTime;

    public Trade(long tradeId, double price, double qty, double eventTime, double ingestionTime) {
        this.tradeId = tradeId;
        this.price = price;
        this.qty = qty;
        this.eventTime = eventTime;
        this.ingestionTime = ingestionTime;
    }

    public long getTradeId() {
        return tradeId;
    }

    public double getPrice() {
        return price;
    }

    public double getQty() {
        return qty;
    }

    public double getEventTime() {
        return eventTime;
    }

    public double getIngestionTime() {
        return ingestionTime;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public void setEventTime(double eventTime) {
        this.eventTime = eventTime;
    }

    public void setIngestionTime(double ingestionTime) {
        this.ingestionTime = ingestionTime;
    }

    public byte[] toByteArray() {
        TradeOuterClass.Trade trade = TradeOuterClass.Trade.newBuilder()
                .setTradeId(this.tradeId)
                .setPrice(this.price)
                .setQty(this.qty)
                .setEventTime(this.eventTime)
                .setIngestionTime(this.ingestionTime)
                .build();

        return trade.toByteArray();
    }

    public static Trade parseFrom(byte[] message) {
        try {
            TradeOuterClass.Trade trade = TradeOuterClass.Trade.parseFrom(message);
            Trade trade_ = new Trade(trade.getTradeId(),
                    trade.getPrice(),
                    trade.getQty(),
                    trade.getEventTime(),
                    trade.getIngestionTime());

            return trade_;

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
    }
}
