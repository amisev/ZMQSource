package sources;

import netscape.javascript.JSObject;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.MultipleIdsMessageAcknowledgingSourceBase;

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.parsing.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * ZeroMQ source (consumer) which reads from a queue
 * @param <OUT> the type of the data read from ZeroMQ
 */
// UID, Session ID
public class ZMQSource<OUT> extends RichSourceFunction<OUT>
        implements ResultTypeQueryable<OUT> {

    private static final long serialVersionUID = -2350020389889300830L;

    private static final Logger LOG = LoggerFactory.getLogger(ZMQSource.class);

    /**
     * consumer properties
     */
    private final ZMQConnectionConfig zmqConnectionConfig;
	private final String queueName;
	// private final boolean usesCorrelationId;
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

		/*
		RuntimeContext runtimeContext = getRuntimeContext();
		if (runtimeContext instanceof StreamingRuntimeContext
				&& ((StreamingRuntimeContext) runtimeContext).isCheckpointingEnabled()) {
			autoAck = false;
			// enables transaction mode
		} else {
			autoAck = true;
		}
		*/

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

	/*
	@Override
	protected void acknowledgeSessionIDs(List<Long> sessionIds) {
		//TODO Acknowledge msgs
		/*try {
			for (long id : sessionIds) {
				channel.basicAck(id, false);
			}
			channel.txCommit();
		} catch (IOException e) {
			throw new RuntimeException("Messages could not be acknowledged during checkpoint creation.", e);
		}
	}
	*/
}
