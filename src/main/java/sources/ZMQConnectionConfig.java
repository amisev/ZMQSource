package sources;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection Configuration for ZMQ.
 */
public class ZMQConnectionConfig implements Serializable {

	private static final long serialVersionUID = -5937499834457457547L;

	private static final Logger LOG = LoggerFactory.getLogger(ZMQConnectionConfig.class);

	private String host;
	private Integer port;
	private String uri;

	public ZMQConnectionConfig(String host, Integer port) {
		this.host = host;
		this.port = port;
		this.uri = "tcp://" + host + ":" + port;
	}

	public ZMQConnectionConfig(String uri) {
		this.uri = uri;
	}

	/** @return the host to use for connections */
	public String getHost() {
		return host;
	}

	/** @return the port to use for connections */
	public int getPort() {
		return port;
	}

	/**
	 * Retrieve the URI.
	 *
	 * @return the connection URI when connecting to the broker
	 */
	public String getUri() {
		return uri;
	}

}
