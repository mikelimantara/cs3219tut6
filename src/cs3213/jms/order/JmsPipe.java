package cs3213.jms.order;

import cs3213.jms.queue.QueueReceive;
import cs3213.jms.queue.QueueReceiveDelegate;
import cs3213.jms.queue.QueueSend;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Matric 1:
 * Name   1:
 * 
 * Matric 2:
 * Name   2:
 *
 * This file implements a pipe that transfer messages using JMS.
 */

public class JmsPipe implements IPipe, QueueReceiveDelegate {
    String listenQueue = null;
    String sendQueue = null;
    String jmsFactory = null;
    QueueSend qs = null;
    QueueReceive qr = null;
    String receivedMessage = null;
    
    // your code here
    JmsPipe(String jmsFactory, String listenQueue, String sendQueue) {
        this.jmsFactory = jmsFactory;
        this.listenQueue = listenQueue;
        this.sendQueue = sendQueue;
    }

    @Override
    public void write(Order s) {
        try {
            if (qs == null) {
                qs = new QueueSend(jmsFactory, sendQueue);
            }

            qs.send(s.toString());
        } catch (Exception e) {
            System.out.println("An error has occurred: " + e.getMessage());
        }
    }

    @Override
    public Order read() {
        try {
            if (qr == null) {
                qr = new QueueReceive(jmsFactory, listenQueue);
                qr.setDelegate(this);
            }

            if (receivedMessage != null) {
                return Order.fromString(receivedMessage);
            }
        } catch (Exception e) {
            System.out.println("An error has occurred: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void close() {
        try {
            if (qs != null) { qs.close(); }
            if (qr != null) { qr.close(); }

        } catch (Exception e) {
            System.out.println("An error has occurred during close: " + e.getMessage());
        }

    }

    @Override
    public void queueReceiveDidReceiveMessage(QueueReceive queueReceive, String message) {
        receivedMessage = message;
    }
}
