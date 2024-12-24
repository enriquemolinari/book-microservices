package model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
class Rating {

    private int totalUserVotes = 0;
    private float rateValue = 0;
    private float totalValue = 0;

    public static Rating notRatedYet() {
        return new Rating();
    }

    public void calculaNewRate(int newUserRate) {
        this.rateValue = Math
                .round(((this.totalValue + newUserRate) / (totalUserVotes + 1))
                        * 100.0f)
                / 100.0f;
        this.totalValue += newUserRate;
        this.totalUserVotes++;
    }

    String actualRateAsString() {
        return String.valueOf(this.rateValue);
    }

    boolean hasValue(float aValue) {
        return this.rateValue == aValue;
    }

    public boolean hastTotalVotesOf(int votes) {
        return this.totalUserVotes == votes;
    }

    int totalVotes() {
        return this.totalUserVotes;
    }
}
