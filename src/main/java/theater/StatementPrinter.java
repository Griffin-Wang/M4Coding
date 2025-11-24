package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

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
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator());

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance p : invoice.getPerformances()) {
            final Play play = plays.get(p.getPlayID());

            final int lineCents = amountCentsFor(p, play);

            // add volume credits
            volumeCredits += Math.max(p.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(play.getType())) {
                volumeCredits += p.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }

            // print line for this order
            final int centsPerDollar = 100;
            result.append(String.format("  %s: %s (%s seats)%n", play.getName(),
                    frmt.format(lineCents / (double) centsPerDollar), p.getAudience()));
            totalAmount += lineCents;
        }
        final int centsPerDollar = 100;
        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / (double) centsPerDollar)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private static int amountCentsFor(Performance per, Play play) {
        int thisAmount = 0;
        switch (play.getType()) {
            case "tragedy": {
                final int tragedyBasePriceCents = 40_000;
                final int tragedyExtraPerAudienceCents = 1_000;
                final int tragedyAudienceThreshold = Constants.TRAGEDY_AUDIENCE_THRESHOLD;
                thisAmount = tragedyBasePriceCents;
                if (per.getAudience() > tragedyAudienceThreshold) {
                    thisAmount += tragedyExtraPerAudienceCents
                            * (per.getAudience() - tragedyAudienceThreshold);
                }
                break;
            }
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (per.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (per.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * per.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return thisAmount;
    }
}
