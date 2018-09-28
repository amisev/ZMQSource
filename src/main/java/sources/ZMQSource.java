package sources;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZeroMQ source (consumer) which reads from a queue
 * @param <OUT> the type of the data read from ZeroMQ
 */
public class ZMQSource<OUT> extends RichSourceFunction<OUT>
        implements ResultTypeQueryable<OUT> {

    private static final long serialVersionUID = -2L;

    private static final Logger LOG = LoggerFactory.getLogger(ZMQSource.class);

    /**
     * consumer properties
     */
    private final ZMQConnectionConfig zmqConnectionConfig;
	private final String queueName;
	protected DeserializationSchema<OUT> schema;

	protected transient boolean autoAck;
	protected transient Context context;
	protected transient Socket frontend;

	private transient volatile boolean running;

	public ZMQSource(ZMQConnectionConfig zmqConnectionConfig, String queueName,
						DeserializationSchema<OUT> deserializationSchema) {
		this.zmqConnectionConfig = zmqConnectionConfig;
		this.queueName = queueName;
		this.schema = deserializationSchema;
	}

	@Override
	public void open(Configuration config) throws Exception {
		super.open(config);

		//  Prepare our context and sockets
		this.context = ZMQ.context(1);
		//  Socket facing clients
		this.frontend = context.socket(ZMQ.PULL);
		// frontend = context.socket(ZMQ.SUB);
		this.frontend.connect(zmqConnectionConfig.getUri());

		LOG.debug("Starting ZeroMQ source with autoAck status: " + autoAck);
		LOG.debug("Starting ZeroMQ source with uri: " + zmqConnectionConfig.getUri());
		running = true;
	}

    public void close() throws Exception {
	    super.close();
	    try {
	        if (this.context != null) {
	            this.context.close();
            }
        } catch (Exception e) {
	        throw new RuntimeException("Error while closing RMQ connection with " + queueName
				+ " at " + zmqConnectionConfig.getHost(), e);
        }
    }

    @Override
    public void run(SourceContext<OUT> sourceContext) throws Exception {
        while (running) {
            OUT result = schema.deserialize(frontend.recv());

            if (schema.isEndOfStream(result)) {
                break;
            }
            sourceContext.collect(result);
        }
    }

    public void cancel() {
		running = false;
	}

	public TypeInformation<OUT> getProducedType() {
		return schema.getProducedType();
	}
}
