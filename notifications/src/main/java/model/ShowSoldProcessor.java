package model;

public class ShowSoldProcessor {
    private final EmailProvider emailProvider;
    private final SaleInfoRequestor saleInfoRequestor;

    public ShowSoldProcessor(EmailProvider emailProvider, SaleInfoRequestor saleInfoRequestor) {
        this.emailProvider = emailProvider;
        this.saleInfoRequestor = saleInfoRequestor;
    }

    public void process(String saleIdentified) {
        var salesInfo = saleInfoRequestor.makeRequest(saleIdentified);
        var email = new NewSaleEmailTemplate(salesInfo.total(),
                salesInfo.username(),
                salesInfo.seats(),
                salesInfo.movieName(),
                salesInfo.showStartTime());
        emailProvider.send(salesInfo.email(), email.subject(), email.body());
    }
}
