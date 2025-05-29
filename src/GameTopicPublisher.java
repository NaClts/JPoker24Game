import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GameTopicPublisher {

	private String host;
	public GameTopicPublisher(String host) throws NamingException, JMSException {
		this.host = host;
		
		// Access JNDI
		createJNDIContext();
		
		// Lookup JMS resources
		lookupTopicConnectionFactory();
		lookupTopic();
		
		// Create connection->session->sender
		createTopicConnection();

        createTopicSession();
		createTopicPublisher();
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
			topicConnection.start();
		} catch (JMSException e) {
			System.err.println("Failed to create topic connection to JMS provider: " + e);
			throw e;
		}
	}

	public void publishMessage(String message) throws JMSException {
        TextMessage textMessage = topicSession.createTextMessage();
        textMessage.setText(message);
        topicPublisher.publish(textMessage);
		System.out.println("Sending message "+message);
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

	private TopicPublisher topicPublisher;
	private void createTopicPublisher() throws JMSException {
		try {
			topicPublisher = topicSession.createPublisher(topic);
		} catch (JMSException e) {
			System.err.println("Failed to create topic publisher: " + e);
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
