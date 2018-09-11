public class Trade {
    private long tradeId;
    private double price;
    private double qty;
    private double eventTime;
    private double ingestionTime;

    public Trade() {};

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

    public void setTradeId(long tradeId) {
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

    @Override
    public String toString() {
        return "TradeId: " + this.tradeId + " "
                + "Price: " + this.price + " "
                + "Quantity: " + this.qty + " "
                + "ETime: " + this.eventTime + " "
                + "ITime: " + this.ingestionTime;
    }
}
