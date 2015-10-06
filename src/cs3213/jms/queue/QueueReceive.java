package cs3213.jms.queue;

import java.util.Properties;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueReceive implements MessageListener {

    public final static String JMS_FACTORY = "SimpleConnectionFactory";
    public final static String QUEUE = "SimpleQueue";

    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueReceiver qreceiver;
    private Queue queue;
    private boolean quit = false;

    public QueueReceive(Context ctx, String queueName) throws NamingException, JMSException {
        this(ctx, JMS_FACTORY, queueName);
    }

    public QueueReceive(Context ctx, String jmsFactory, String queueName) throws NamingException, JMSException {
        qconFactory = (QueueConnectionFactory) ctx.lookup(jmsFactory);
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) ctx.lookup(queueName);
        qreceiver = qsession.createReceiver(queue);
        qreceiver.setMessageListener(this);
        qcon.start();
    }


    @Override
    public void onMessage(Message msg) {
        try {
            String msgText;
            if (msg instanceof TextMessage) {
                msgText = ((TextMessage) msg).getText();
            } else {
                msgText = msg.toString();
            }

            System.out.println("Message Received: " + msgText);

            if (msgText.equalsIgnoreCase("quit")) {
                synchronized (this) {
                    quit = true;
                    this.notifyAll(); // ask main thread to quit
                }
            }
        } catch (JMSException jmse) {
            System.err.println("An exception occurred: " + jmse.getMessage());
        }
    }

    public void close() throws JMSException {
        qreceiver.close();
        qsession.close();
        qcon.close();
    }

    public static void main(String[] args) throws Exception {        
        InitialContext ic = getInitialContext();
        QueueReceive qr = new QueueReceive(ic, QUEUE);

        System.out.println(
                "JMS Ready To Receive Messages (To quit, send a \"quit\" message).");
        
        synchronized (qr) {
            while (!qr.quit) {
                try {
                    qr.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        qr.close();
    }

    private static InitialContext getInitialContext()
            throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        return new InitialContext(props);
    }
}
