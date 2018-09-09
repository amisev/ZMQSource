import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import sources.TradeOuterClass;
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

        // get input data by connecting to the socket
        // DataStream<String> text = env.socketTextStream(hostname, port, "\n");

        // zmq config
        ZMQConnectionConfig config = new ZMQConnectionConfig("127.0.0.1", 5559);
        // deserialization schema
        ZMQSourceSchema schema = new ZMQSourceSchema();
        // zmq source
        ZMQSource<Trade> source = new ZMQSource<>(config, "trades", schema);

        DataStream<Trade> trades = env.addSource(source);

        // parse the data, group it, window it, and aggregate the counts
        DataStream<Double> aggVolumes = trades
                .timeWindowAll(Time.seconds(10))
                .aggregate(new SummingAggregator());

        // print the results with a single thread, rather than in parallel
        aggVolumes.print().setParallelism(1);

        env.execute("Socket Window WordCount");
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
