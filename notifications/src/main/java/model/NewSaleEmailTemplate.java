package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NewSaleEmailTemplate {

    public static final String EMAIL_SUBJECT_SALE = "You have new tickets!";
    private final float totalAmount;
    private final String userName;
    private final List<Integer> seatsBought;
    private final String movieName;
    private final String showStartTime;

    public NewSaleEmailTemplate(float totalAmount, String userName,
                                Set<Integer> seatsBought, String movieName, String showStartTime) {
        this.totalAmount = totalAmount;
        this.userName = userName;
        this.seatsBought = new ArrayList<>(seatsBought);
        Collections.sort(this.seatsBought);
        this.movieName = movieName;
        this.showStartTime = showStartTime;
    }

    public String subject() {
        return EMAIL_SUBJECT_SALE;
    }

    public String body() {
        return "Hello " + userName + "!" +
                System.lineSeparator() +
                "You have new tickets!" +
                System.lineSeparator() +
                "Here are the details of your booking:" +
                System.lineSeparator() +
                "Movie: " + movieName +
                System.lineSeparator() +
                "Seats: " + seatsBought.stream()
                .map(Object::toString).collect(Collectors.joining(",")) +
                System.lineSeparator() +
                "Show time: " + showStartTime +
                System.lineSeparator() +
                "Total paid: " + totalAmount;
    }
}
