import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TwentyFourChecker {

    public static boolean isExpressionEqualTo24(String expression, String[] validNumbers) {
        // Remove all whitespace
        String processed = expression.replaceAll("\\s+", "");

        processed = processed.replaceAll("a", "A");
        processed = processed.replaceAll("j", "J");
        processed = processed.replaceAll("q", "Q");
        processed = processed.replaceAll("k", "K");

        if ( ! containsValidNumbersAndOperators(processed, validNumbers) ) {
            return false;
        }
        
        // Replace face cards with their numeric values
        processed = processed.replaceAll("A", "1");
        processed = processed.replaceAll("J", "11");
        processed = processed.replaceAll("Q", "12");
        processed = processed.replaceAll("K", "13");
        
        // Evaluate the expression
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("js");
            
            // Evaluate as double to handle fractions
            Object result = engine.eval(processed);
            
            if (result instanceof Number) {
                double value = ((Number) result).doubleValue();
                // Use epsilon for floating point comparison
                return Math.abs(value - 24.0) < 1e-9;
            }
            return false;
        } catch (ScriptException e) {
            // If evaluation fails (syntax error), return false
            return false;
        }
    }

    private static boolean containsValidNumbersAndOperators(String expression, String[] validNumbers) {

        expression = expression.replaceAll("\\s+", "");

        int numOfNotMatched = 4;
        for (String validNumber : validNumbers) {
            if (expression.contains(validNumber)) {
                expression = expression.replaceFirst(validNumber, "");
                numOfNotMatched--;
            }
        }
        if (numOfNotMatched != 0) {
            return false;
        }

        expression = expression.replaceAll("[+\\-*/()]", "");

        if (expression.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}