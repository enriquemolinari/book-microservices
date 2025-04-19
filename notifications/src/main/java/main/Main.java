package main;

import model.SaleInfoRequestor;
import model.ShowSoldProcessor;
import model.TheBestEmailProvider;
import model.queue.RabbitConnStr;
import model.queue.RabbitMQBroker;

public class Main {
    public static void main(String[] args) {
        new Main().startUp();
    }

    public void startUp() {
        var config = new Config("default");
        var url = config.uriScheme() + config.gatewayHost() + ":" + config.gatewayPort() + config.salesRequestPath();
        var rabbitConn = new RabbitConnStr(config.rabbitMQHost(), config.rabbitMQUsername(), config.rabbitMQPassword(), config.queueName());
        var requestor = new SaleInfoRequestor(url);
        var processor = new ShowSoldProcessor(new TheBestEmailProvider(), requestor);
        var rabbitBroker = new RabbitMQBroker(rabbitConn, processor);
        rabbitBroker.listen();
    }
}
