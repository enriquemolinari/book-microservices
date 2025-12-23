package model;

public class ForTests {

    public String expectedShowSoldBody() {
        return """
                Hello emolinari!
                You have new tickets!
                Here are the details of your booking:
                Movie: The Movie of the Century
                Seats: 3
                Show time: Tuesday 04/08 20:56
                Total paid: 10.0""";
    }

    public String jsonShowSale() {
        return
                """                        
                        {
                                         "salesIdentifier": "7f1ffef0-f0d4-4099-a368-d5e76c652a8b",
                                         "movieId": 2,
                                         "movieName": "The Movie of the Century",
                                         "userId": 2,
                                         "username": "emolinari",
                                         "fullname": "Enrique Molinari",
                                         "email": "enrique.molinari@gmail.com",
                                         "total": 10.0,
                                         "pointsWon": 10,
                                         "seats": [
                                            3
                                         ],
                                         "showStartTime": "Tuesday 04/08 20:56"
                                         }
                        """;
    }

    public String getUrl(String host, int port, String requestPath) {
        return "http://" + host + ":" + port + requestPath;
    }
}
