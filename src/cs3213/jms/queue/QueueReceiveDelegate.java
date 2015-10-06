package cs3213.jms.queue;

public interface QueueReceiveDelegate {
    void queueReceiveDidReceiveMessage(QueueReceive queueReceive, String message);
}
