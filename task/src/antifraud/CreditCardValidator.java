package antifraud;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CreditCardValidator implements ConstraintValidator<CreditCard, String> {

    @Override
    public boolean isValid(String cardNumber, ConstraintValidatorContext context) {
        int sum = 0;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cardNumber.substring(i, i + 1));

            if ((cardNumber.length() - i) % 2 == 0) {
                digit = doubleAndSumDigits(digit);
            }

            sum += digit;
        }
        return sum % 10 == 0;
    }

    private static int doubleAndSumDigits(int digit) {
        int ret = digit * 2;

        if (ret > 9) {
            ret = digit - 9;
        }

        return ret;
    }
}
