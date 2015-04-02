package studio.idle.mathduel.common;

import java.util.List;
import java.util.Random;

/**
 * Created by Udit on 31/03/15.
 */
public class FormulaHelper {
    public static Random rand;
    public static int gameDifficultyLevel;

    public static Formula fetchFormula(int gameLevel) {
        gameDifficultyLevel = gameLevel;
        rand = new Random();
        Formula formula = null;
        switch (gameLevel) {
            case 0:
                formula = createFormula(CommonConstants.minEasyValue, CommonConstants.maxEasyValue, CommonConstants.symbolListEasy);
                break;
            case 1:
                formula = createFormula(CommonConstants.minMediumValue, CommonConstants.maxMediumValue, CommonConstants.symbolListMedium);
                break;
            case 2:
                formula = createFormula(CommonConstants.minHardValue, CommonConstants.maxHardValue, CommonConstants.symbolListDifficult);
        }
        return formula;
    }

    public static Formula createFormula(int minValue, int maxValue, List<String> symbolList) {
        Formula formula = new Formula();
        formula.operator = fetchOperator(symbolList);
        formula.firstOperand =fetchFirstOperand(formula.operator, maxValue, minValue);
        formula.secondOperand = fetchSecondOperand(formula.operator, formula.firstOperand, maxValue, minValue);
        return formula;
    }

    public static String fetchOperator(List<String> symbolList) {
        return symbolList.get(rand.nextInt(symbolList.size()));
    }

    public static int fetchFirstOperand(String operand, int maxValue, int minValue) {
        if (operand.equals("/")) {
            return rand.nextInt((maxValue - minValue) + 1) + (minValue + 2); // Minimum value to come as 2 if minValue is zero
        } else {
            return rand.nextInt((maxValue - minValue) + 1) + minValue;
        }
    }

    public static int fetchSecondOperand(String operand, int firstOperand, int maxValue, int minValue) {
        if (operand.equals("-") && gameDifficultyLevel == 0) {
            return rand.nextInt((firstOperand - minValue) + 1) + minValue; // answer should not be negative in Easy Case
        } else if (operand.equals("*")) {
            return rand.nextInt(((firstOperand / 10) - minValue) + 1) + minValue; // The number does not go very high in case of multiply
        } else if (operand.equals("/")) {
            int number = rand.nextInt((firstOperand - minValue) + 1) + minValue + 1; // Minimum value to come as 1 and max value to be that number

            while (firstOperand % number != 0) {
                number = rand.nextInt((firstOperand - minValue) + 1) + minValue + 1;
            }
            return number;
        } else {
            return rand.nextInt((maxValue - minValue) + 1) + minValue;
        }
    }

    public static int calculateAnswer(int firstOperand, int secondOperand, String operator) {
        switch (operator) {
            case "+":
                return firstOperand + secondOperand;
            case "-":
                return firstOperand - secondOperand;
            case "*":
                return firstOperand * secondOperand;
            case "/":
                return firstOperand / secondOperand;
        }
        return 0; // To remove error
    }
}
