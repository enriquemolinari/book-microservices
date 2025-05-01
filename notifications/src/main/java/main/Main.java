package main;

import model.NewTicketSoldProcessor;
import model.SaleInfoRequestor;
import model.TheBestEmailProvider;
import model.queue.RabbitConnStr;
import model.queue.RabbitMQBroker;

public class Main {
    private Config config;

    public Main() {
        config = new Config("default");
    }

    public static void main(String[] args) {
        new Main().startUp();
    }

    public void startUp() {
        var rabbitConn = new RabbitConnStr(config.rabbitMQHost()
                , config.rabbitMQUsername()
                , config.rabbitMQPassword()
                , config.queueName());
        var processor = new NewTicketSoldProcessor(
                new TheBestEmailProvider(),
                new SaleInfoRequestor(getUrl(config)));
        new RabbitMQBroker(rabbitConn, processor).listenForNewTickets();
    }

    private String getUrl(Config config) {
        return config.uriScheme() + config.gatewayHost() + ":" + config.gatewayPort() + config.salesRequestPath();
    }
}
