import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Named after the Belgian mathematician Edouard Zeckendorf, Zeckendorf's theorem
 * gives a representation of integers as sums of Fibonacci numbers.
 * Zeckendorf's theorem states that every positive integer can be represented uniquely
 * as the sum of one or more distinct Fibonacci numbers in such a way
 * that the sum does not include any two consecutive Fibonacci numbers.
 *
 * This class represents an immutable arbitrary-precision integer.
 * All operations behave as if Zeckendorf integers were represented as a Zeckendorf bits.
 * Meaning that the arithmetic operations are performed on the bits, and not the
 * decimal (radix 10) representation of the numbers.
 *
 * This class is modeled after the BigInteger class.
 *
 * Zeckendorf provides arithmetic operations based on the algorithms defined in two papers
 *
 * Fenwick, P. (2003). Zeckendorf integer arithmetic. Fibonacci Quarterly, 41(5), 405-413.
 *
 * Ahlbach, C., Usatine, J., & Pippenger, N. (2012). Efficient algorithms for Zeckendorf arithmetic.
 *
 * @author Shane Carroll May
 */
public final class Zeckendorf extends Number implements Comparable<Zeckendorf> {

    public static final Zeckendorf ZERO = new Zeckendorf("0");

    public static final Zeckendorf ONE = new Zeckendorf("1");
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(Zeckendorf.class.getName());

    /**
     * In the Zeckendorf representation of the integers, representation usually
     * omits the redundant bit corresponding to F(1) = 1, so that the
     * least-significant bit corresponds to F(2) which is also 1. This offset represents
     * that omission of F(1).
     */
    private static final int FIBONACCI_OFFSET = 2;

    /**
     * The bits of this ZeckendorfInteger, in <i>big-endian</i> order: the
     * zeroth element of this List is the most-significant int of the
     * magnitude.
     */
    private List<Byte> bits = new ArrayList<>();

    /**
     * The signum of this Zeckendorf Integer:
     * -1 for negative, 0 for zero, or 1 for positive.
     * Note - We say that the Zeckendorf zero must have a signum of 0.
     */
    private int signum;

    /**
     * Generates the nth term of the Fibonacci number sequence.
     *
     * @param term the nth term to generate.
     *
     * @return the nth term of the sequence.
     */
    private static BigInteger fibonacci(int term) {
        // TODO - Consider caching the number sequence up to Long.MAX_VALUE.
        if (term < FIBONACCI_OFFSET) {
            return new BigInteger(String.valueOf(term));
        } // else, we need to calculate the term doNothing();

        var result = BigInteger.ZERO;
        var n1 = BigInteger.ZERO;
        var n2 = BigInteger.ONE;
        for(term--;term > 0; term--) {
            result = n1.add(n2);
            n1 = n2;
            n2 = result;
        }
        return result;
    }

    /**
     * Parse a {@code BigInteger} into its Zeckendorf representation.
     *
     * @param value the {@code BigInteger} to parse.
     */
    private void parseToZeckendorfBits(BigInteger value)
    {
        this.signum = value.signum();
        var workingCopy = value.abs();

        List<BigInteger> fibonacciNumbers = new ArrayList<>();
        var step = FIBONACCI_OFFSET;
        var fibonacciNumber = fibonacci(step);
        while (fibonacciNumber.compareTo(workingCopy) <= 0) {
            fibonacciNumbers.add(fibonacciNumber);
            step++;
            fibonacciNumber = fibonacci(step);
        }

        for (int index = fibonacciNumbers.size() - 1; index >= 0; index--) {
            fibonacciNumber = fibonacciNumbers.get(index);
            if (fibonacciNumber.compareTo(workingCopy) <= 0) {
                this.bits.add((byte) 1);
                workingCopy = workingCopy.subtract(fibonacciNumber);
            } else {
                this.bits.add((byte) 0);
            }
        }
    }

//    public Zeckendorf(byte[] bits) {
//
//    }

    /**
     * Constructs a new instance of a Zeckendorf integer.
     * The String representation may consists of an optional minus sign followed by a
     * sequence of one or more decimal digits.
     *
     * @param number a string representation of an integer (Radix 10).
     *
     * @throws NumberFormatException - if the number is not a valid number.
     */
    public Zeckendorf(String number) {
        this.parseToZeckendorfBits(new BigInteger(number));
    }

    /**
     * Returns a Zeckendorf integer whose value is (this + augend).
     *
     * @param augend value to be added to this instance.
     *
     * @return this + augend
     */
    public Zeckendorf add(Zeckendorf augend) {
        return ZERO;
    }

    /**
     * Returns a string representation of this instance as the Zeckendorf and integer
     * representation.
     *
     * @return a string of the bits along with the integer representation.
     */
    @Override
    public String toString() {
        return String.format("%d ~ %s | %d", this.signum, this.bits, this.longValue());
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     */
    @Override
    public int compareTo(Zeckendorf o) {
        return 0;
    }

    /**
     * Returns the value of the specified number as an {@code int}.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code int}.
     */
    @Override
    public int intValue() {
        return 0;
    }

    /**
     * Returns the value of the specified number as a {@code long}.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code long}.
     */
    @Override
    public long longValue() {
        return this.bigIntegerValue().longValue();
    }

    /**
     * Returns the value of the specified number as a {@code BigInteger}.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code BigInteger}
     */
    public BigInteger bigIntegerValue() {
        var value = BigInteger.ZERO;
        for (int index = 0; index < this.bits.size(); index++) {
            if (this.bits.get(index) == 1) {
                // decimal 38 = 34 + 3 + 1
                // numbers = 0,1,1,2,3,5,8,13,21,34
                // offset is two to ignore the first two trivial cases of 0 and duplicate 1.
                // 38 as zeckendorf = 10000101
                // index 0 = fibonacci nine so size => 8 - index (0) - 1 + 2
                // 8 - 0 - 1 + 2 = 9
                var fibonacciStep = this.bits.size() - index - 1 + FIBONACCI_OFFSET;
                value = value.add(fibonacci(fibonacciStep));
            } // else 0 bit nothing to add doNothing();
        }
        return this.signum < 0 ? value.negate() : value;
    }

    /**
     * Returns the value of the specified number as a {@code float}.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code float}.
     */
    @Override
    public float floatValue() {
        return 0;
    }

    /**
     * Returns the value of the specified number as a {@code double}.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code double}.
     */
    @Override
    public double doubleValue() {
        return 0;
    }
}
