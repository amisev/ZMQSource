import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;

import sources.ZMQConnectionConfig;
import sources.ZMQSource;

@SuppressWarnings("serial")
public class SocketTradesCount {

    public static void main(String[] args) throws Exception {

        // the host and the port to connect to
        final String hostname;
        final int port;
        try {
            final ParameterTool params = ParameterTool.fromArgs(args);
            hostname = params.has("hostname") ? params.get("hostname") : "127.0.0.1";
            port = params.getInt("port");
        } catch (Exception e) {
            System.err.println("No port specified. Please run 'SocketWindowWordCount " +
                    "--hostname <hostname> --port <port>', where hostname (localhost by default) " +
                    "and port is the address of the text server");
            System.err.println("To start a simple text server, run 'netcat -l <port>' and " +
                    "type the input text into the command line");
            return;
        }

        // get the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        // zmq config
        ZMQConnectionConfig config = new ZMQConnectionConfig("127.0.0.1", 5559);
        // deserialization schema
        ZMQSourceSchema schema = new ZMQSourceSchema();
        // zmq source
        ZMQSource<Trade> source = new ZMQSource<>(config, "trades", schema);

        DataStream<Trade> trades = env.addSource(source).setParallelism(1);

        DataStream<Trade> tradesWTSW = trades.assignTimestampsAndWatermarks(
                new AscendingTimestampExtractor<Trade>() {
                    @Override
                    public long extractAscendingTimestamp(Trade trade) {
                        return (long)trade.getEventTime()*1000;
                    }
                }
        );

        DataStream<Double> volumes = tradesWTSW
                .map(trade -> trade.getQty())
                .timeWindowAll(Time.seconds(5))
                .sum("0");

        volumes.print();

        // trades.print();

        env.execute("Trades agg volumes");
    }
    private static class SummingAggregator implements AggregateFunction<Trade, Double, Double> {

        public Double add(Trade trade, Double aDouble) {
            return aDouble + trade.getQty();
        }

        public Double createAccumulator() {
            return 0.0;
        }

        public Double getResult(Double aDouble) {
            return aDouble;
        }

        public Double merge(Double aDouble, Double acc1) {
            return aDouble + acc1;
        }
    }

    // ------------------------------------------------------------------------

}
