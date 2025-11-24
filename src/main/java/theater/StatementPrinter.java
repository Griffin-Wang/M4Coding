package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private static final int CENTS_PER_DOLLAR = 100;
    private Invoice invoice;
    private Map<String, Play> plays;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    public void setPlays(Map<String, Play> plays) {
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {

        int totalCents = 0;
        int volumeCredits = 0;
        final StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance p : invoice.getPerformances()) {
            final int lineCents = getAmount(p);

            volumeCredits += getVolumeCredits(p);

            // print line for this order
            result.append(lineLine(getPlay(p).getName(), lineCents, p.getAudience()));
            totalCents += lineCents;
        }
        result.append(totalLine(totalCents));
        result.append(creditsLine(volumeCredits));
        return result.toString();
    }

    private int getVolumeCredits(Performance performance) {
        final Play play = getPlay(performance);

        int credits = Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);

        if ("comedy".equals(play.getType())) {
            credits += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return credits;
    }

    private int getAmount(Performance performance) {
        int thisAmount = 0;
        switch (getPlay(performance).getType()) {
            case "tragedy": {
                final int tragedyBasePriceCents = 40_000;
                final int tragedyExtraPerAudienceCents = 1_000;
                final int tragedyAudienceThreshold = Constants.TRAGEDY_AUDIENCE_THRESHOLD;
                thisAmount = tragedyBasePriceCents;
                if (performance.getAudience() > tragedyAudienceThreshold) {
                    thisAmount += tragedyExtraPerAudienceCents
                            * (performance.getAudience() - tragedyAudienceThreshold);
                }
                break;
            }
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", getPlay(performance).getType()));
        }
        return thisAmount;
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private String usd(int cents) {
        return currency.format(cents / (double) CENTS_PER_DOLLAR);
    }

    private String lineLine(String playName, int cents, int audience) {
        return String.format("  %s: %s (%s seats)%n", playName, usd(cents), audience);
    }

    private String totalLine(int totalCents) {
        return String.format("Amount owed is %s%n", usd(totalCents));
    }

    private String creditsLine(int credits) {
        return String.format("You earned %s credits%n", credits);
    }

    private int getTotalAmount() {
        int result = 0;
        for (Performance p : invoice.getPerformances()) {
            result += getAmount(p);
        }
        return result;
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance p : invoice.getPerformances()) {
            result += getVolumeCredits(p);
        }
        return result;
    }

}
