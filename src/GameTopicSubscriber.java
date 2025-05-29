import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GameTopicSubscriber {

	private String host;
	public GameTopicSubscriber(String host) throws NamingException, JMSException {
		this.host = host;
		
		// Access JNDI
		createJNDIContext();
		
		// Lookup JMS resources
		lookupTopicConnectionFactory();
		lookupTopic();
		
		// Create connection->session->sender
		createTopicConnection();

        createTopicSession();
		createTopicSubscriber();
	}

    private Context jndiContext;
	private void createJNDIContext() throws NamingException {
		System.setProperty("org.omg.CORBA.ORBInitialHost", host);
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("Could not create JNDI API context: " + e);
			throw e;
		}
	}

	private TopicConnectionFactory topicConnectionFactory;
	private void lookupTopicConnectionFactory() throws NamingException {

		try {
			topicConnectionFactory = (TopicConnectionFactory)jndiContext.lookup("jms/JPoker24GameTopicConnectionFactory");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS topic connection factory lookup failed: " + e);
			throw e;
		}
	}

    private Topic topic;
	private void lookupTopic() throws NamingException {

		try {
			topic = (Topic)jndiContext.lookup("jms/JPoker24GameTopic");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS topic lookup failed: " + e);
			throw e;
		}
	}

    private TopicConnection topicConnection;
	private void createTopicConnection() throws JMSException {
		try {
			topicConnection = topicConnectionFactory.createTopicConnection();
		} catch (JMSException e) {
			System.err.println("Failed to create topic connection to JMS provider: " + e);
			throw e;
		}
	}

	private TopicSession topicSession;
	private void createTopicSession() throws JMSException {
		try {
			topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			System.err.println("Failed to create topic session: " + e);
			throw e;
		}
	}

	private TopicSubscriber topicSubscriber;
	private void createTopicSubscriber() throws JMSException {
		try {
			topicSubscriber = topicSession.createSubscriber(topic);
		} catch (JMSException e) {
			System.err.println("Failed to create topic subscriber: " + e);
			throw e;
		}
	}

	public void setMessageListener(MessageListener topicListener) throws JMSException {
		try {
			topicSubscriber.setMessageListener(topicListener);
		} catch (JMSException e) {
			System.err.println("Failed to set message listener: " + e);
			throw e;
		}
	}

	public void start() throws JMSException {
		try {
			topicConnection.start();
		} catch (JMSException e) {
			System.err.println("Failed to create topic connection to JMS provider: " + e);
			throw e;
		}
	}

	public void close() {
		if(topicConnection != null) {
			try {
				topicConnection.close();
			} catch (JMSException e) { }
		}
	}
}
