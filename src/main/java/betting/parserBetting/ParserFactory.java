package betting.parserBetting;

public class ParserFactory {

    public static BettingParser getParser(BettingParser parser) {
        BettingParser parserToReturn = null;
        if (parser.getClass() == BettingParserBet365.class) {
            parserToReturn = new BettingParserBet365();
        } else if (parser.getClass() == BettingParserBetSBC.class) {
            parserToReturn = new BettingParserBetSBC();
        } else if (parser.getClass() == BettingParserPariMatchOldVersion.class) {
            parserToReturn = new BettingParserPariMatchOldVersion();
        } else if (parser.getClass() == BettingParserWilliamHill.class) {
            parserToReturn = new BettingParserWilliamHill();
        } else if (parser.getClass() == BettingBetFair.class) {
            parserToReturn = new BettingBetFair();
        }
        return parserToReturn;
    }

}
