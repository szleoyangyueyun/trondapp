package shop.tronlucky.trondapp.common.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import org.apache.commons.lang3.ArrayUtils;

public class Base58 {

  public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
      .toCharArray();
  private static final int[] INDEXES = new int[128];

  static {
    for (int i = 0; i < INDEXES.length; i++) {
      INDEXES[i] = -1;
    }
    for (int i = 0; i < ALPHABET.length; i++) {
      INDEXES[ALPHABET[i]] = i;
    }
  }

  /**
   * Encodes the given bytes in base58. No checksum is appended.
   */
  public static String encode(byte[] input) {
    if (input.length == 0) {
      return "";
    }
    input = copyOfRange(input, 0, input.length);
    // Count leading zeroes.
    int zeroCount = 0;
    while (zeroCount < input.length && input[zeroCount] == 0) {
      ++zeroCount;
    }
    // The actual encoding.
    byte[] temp = new byte[input.length * 2];
    int j = temp.length;

    int startAt = zeroCount;
    while (startAt < input.length) {
      byte mod = divmod58(input, startAt);
      if (input[startAt] == 0) {
        ++startAt;
      }
      temp[--j] = (byte) ALPHABET[mod];
    }

    // Strip extra '1' if there are some after decoding.
    while (j < temp.length && temp[j] == ALPHABET[0]) {
      ++j;
    }
    // Add as many leading '1' as there were leading zeros.
    while (--zeroCount >= 0) {
      temp[--j] = (byte) ALPHABET[0];
    }

    byte[] output = copyOfRange(temp, j, temp.length);
    try {
      return new String(output, "US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);  // Cannot happen.
    }
  }

  public static byte[] decode(String input) throws IllegalArgumentException {
    if (input.length() == 0) {
      return new byte[0];
    }
    byte[] input58 = new byte[input.length()];
    // Transform the String to a base58 byte sequence
    for (int i = 0; i < input.length(); ++i) {
      char c = input.charAt(i);

      int digit58 = -1;
      if (c >= 0 && c < 128) {
        digit58 = INDEXES[c];
      }
      if (digit58 < 0) {
        throw new IllegalArgumentException("Illegal character " + c + " at " + i);
      }

      input58[i] = (byte) digit58;
    }
    // Count leading zeroes
    int zeroCount = 0;
    while (zeroCount < input58.length && input58[zeroCount] == 0) {
      ++zeroCount;
    }
    // The encoding
    byte[] temp = new byte[input.length()];
    int j = temp.length;

    int startAt = zeroCount;
    while (startAt < input58.length) {
      byte mod = divmod256(input58, startAt);
      if (input58[startAt] == 0) {
        ++startAt;
      }

      temp[--j] = mod;
    }
    // Do no add extra leading zeroes, move j to first non null byte.
    while (j < temp.length && temp[j] == 0) {
      ++j;
    }

    return copyOfRange(temp, j - zeroCount, temp.length);
  }

  public static BigInteger decodeToBigInteger(String input) throws IllegalArgumentException {
    return new BigInteger(1, decode(input));
  }

  /**
   *  number -> number / 58, returns number % 58
   */
  private static byte divmod58(byte[] number, int startAt) {
    int remainder = 0;
    for (int i = startAt; i < number.length; i++) {
      int digit256 = (int) number[i] & 0xFF;
      int temp = remainder * 256 + digit256;

      number[i] = (byte) (temp / 58);

      remainder = temp % 58;
    }

    return (byte) remainder;
  }

  /**
   * number -> number / 256, returns number % 256
   */
  private static byte divmod256(byte[] number58, int startAt) {
    int remainder = 0;
    for (int i = startAt; i < number58.length; i++) {
      int digit58 = (int) number58[i] & 0xFF;
      int temp = remainder * 58 + digit58;

      number58[i] = (byte) (temp / 256);

      remainder = temp % 256;
    }

    return (byte) remainder;
  }

  private static byte[] copyOfRange(byte[] source, int from, int to) {
    byte[] range = new byte[to - from];
    System.arraycopy(source, from, range, 0, range.length);

    return range;
  }

  public static byte[] hexs2Bytes(byte[] a) {
    if (ArrayUtils.isEmpty(a)) {
      return null;
    }
    if ((a.length & 0x01) != 0) {
      return null;
    }
    byte[] result = new byte[a.length / 2];
    for (int i = 0; i < result.length; i++) {
      byte h = a[i * 2];
      byte l = a[i * 2 + 1];
      if (h >= '0' && h <= '9') {
        result[i] = (byte) ((h - '0') << 4);
      } else if (h >= 'a' && h <= 'f') {
        result[i] = (byte) ((h - 'a' + 10) << 4);
      } else if (h >= 'A' && h <= 'F') {
        result[i] = (byte) ((h - 'A' + 10) << 4);
      } else {
        return null;
      }

      if (l >= '0' && l <= '9') {
        result[i] += (l - '0');
      } else if (l >= 'a' && l <= 'f') {
        result[i] += (l - 'a' + 10);
      } else if (l >= 'A' && l <= 'F') {
        result[i] += (l - 'A' + 10);
      } else {
        return null;
      }
      h = l = 0;
    }
    return result;
  }

  public static boolean priKeyValid(byte[] priKey) {
    if (ArrayUtils.isEmpty(priKey)) {
      return false;
    }
    if (priKey.length != 32) {
      return false;
    }
    //Other rule;
    return true;
  }
}