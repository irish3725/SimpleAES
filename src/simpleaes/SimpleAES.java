package simpleaes;

import java.io.UnsupportedEncodingException;

/**
 * A simplified version of AES algorithm. In this version, 
 * only the first three steps are performed in every rotation.
 * @author alex
 */
public class SimpleAES {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException {

        String input = "abcdefghijklmnop";
        int[] sbox = getSBox();
        int[] tran_matrix = {2, 3, 1, 1, 1, 2, 3, 1, 1, 1, 2, 3, 3,
            1, 1, 2};

        if (args.length > 0) {
            input = args[0];
        }

        byte[] key = input.getBytes("UTF-8");
        key = columnMajor(key);

        System.out.println("The plaintext is:");
        char[] cInput = input.toCharArray();
        System.out.println("input bytes: ");
        printColumnBytes(key);
        for (char c : cInput) {
            System.out.print(c + " ");
        }
        System.out.println("\n------------------------------------");
        for (int i = 0; i < 10; i++) {
            key = subBytes(key, sbox);
            key = shiftRows(key);
            if (i < 9) {
                key = mixColumns(key, tran_matrix);
            }
            System.out.println("After " + (i + 1) + " round(s), the state is");
//            printColumnBytes(key);
            printMatrix(key);

        }
    }

    /**
     * Subs bytes from sbox into key and returns key using shiftBytes algorithm.
     *
     * Uses getBits() to get the individual bits in each byte Uses getX() and
     * getY() to get index into sbox
     * @param k
     * @return
     */
    public static byte[] subBytes(byte[] k, int[] sbox) {
        byte[] nKey = new byte[16];
        int[] bits;
        int x;
        int y;
        int sboxVal;

        for (int i = 0; i < 16; i++) {
            bits = getBits(k[i]);
            x = getX(bits);
            y = getY(bits);
            sboxVal = sbox[(16 * y) + x];
            nKey[i] = (byte) (sboxVal & (0xff));
        }
        return nKey;
    }

    /**
     * Shifts the first row by 0, second by 1, 3rd by 2, and 4th by 3 using the
     * shiftRow method on each row.
     * @param k
     * @return
     */
    public static byte[] shiftRows(byte[] k) {
        byte[] key = k;
        byte[] keyPart = new byte[4];

        for (int i = 1; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                keyPart[j] = key[4 * i + j];
            }
            keyPart = shiftRow(keyPart, i);
            for (int j = 0; j < 4; j++) {
                key[4 * i + j] = keyPart[j];
            }
        }

        return key;
    }

    /**
     * This operation uses matrix multiplication on the key with
     * the GF8 matrix. 
     * @param key
     * @param tran_matrix
     * @return 
     */
    public static byte[] mixColumns(byte[] key, int[] tran_matrix) {
        byte[] nKey = new byte[16];
        int[] row = new int[4];
        int[] column = new int[4];

        for (int i = 0; i < 4; i++) {
            row[0] = tran_matrix[(4 * i) + 0];
            row[1] = tran_matrix[(4 * i) + 1];
            row[2] = tran_matrix[(4 * i) + 2];
            row[3] = tran_matrix[(4 * i) + 3];
            for (int j = 0; j < 4; j++) {
                column[0] = (int)key[j];
                column[1] = (int)key[4 + j];
                column[2] = (int)key[8 + j];
                column[3] = (int)key[12 + j];
                nKey[(4 * i) + j] = multRC(row,column);
            }

        }

        return nKey;
    }

    //////////////////////////////
    //      Helper Methods      //
    //////////////////////////////

    /**
     * Multiplies a single row with a single column
     * @param row
     * @param column
     * @return 
     */
    public static byte multRC(int[] row, int[] column) {
        int[] nColumn = new int[4];
        boolean overflow = false;
        int result = 0;

        for (int i = 0; i < 4; i++) {
            overflow = false;
            if (column[i] > 127) {
                overflow = true;
            }
            if (row[i] == 1) {
                nColumn[i] = column[i];
            }
            if (row[i] == 2) {
                nColumn[i] = (column[i] * 2);
                if (overflow) {
                    nColumn[i] = (column[i] * 2) ^ 27;
                }
            }
            if (row[i] == 3) {
                if (overflow) {
                    nColumn[i] = column[i] ^ ((column[i] * 2) ^ 27);
                } else {
                    nColumn[i] = column[i] ^ (column[i] * 2);
                }
            }
        }
        result = nColumn[0] ^ nColumn[1];
        result = result ^ nColumn[2];
        result = result ^ nColumn[3];
        return (byte) result;
    }
    
    /**
     * Returns x value based on subBytes algorithm using bits 7,6,5,4
     * @TODO: could be backwards
     * @param bits
     * @return
     */
    public static int getX(int[] bits) {
        int x = (8 * bits[7]) + (4 * bits[6])
                + (2 * bits[5]) + bits[4];
        return x;
    }

    /**
     * Returns y value based on subBytes algorithm using bits 3,2,1,0
     * @param bits
     * @return
     */
    public static int getY(int[] bits) {
        int y = (8 * bits[3]) + (4 * bits[2])
                + (2 * bits[1]) + bits[0];
        return y;
    }

    /**
     * Returns an int array of the bits in inputted byte
     * @param b
     * @return
     */
    public static int[] getBits(byte b) {
        int[] bits = new int[8];

        for (int i = 0; i < 8; i++) {
            int bit = (b & (1 << i));
            if (bit == 0) {
                bits[i] = 0;
            } else {
                bits[i] = 1;
            }
        }

        return bits;
    }

    /**
     * Does the shifting for a single row in the byte matrix.
     * @param b
     * @param offset
     * @return
     */
    public static byte[] shiftRow(byte[] b, int offset) {
        byte[] temp = new byte[4];
        for (int i = 0; i < 4; i++) {
            temp[i] = b[(i + offset) % 4];
        }
        return temp;
    }
    
    /**
     * Flips the matrix over diagonal to turn it from row
     * major to colum major.
     * @param key
     * @return 
     */
    public static byte[] columnMajor(byte[] key) {
        byte[] nKey = new byte[16];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                nKey[(4 * i) + j] = key[(4 * j) + i];
            }
        }

        return nKey;
    }

    /**
     * Prints Byte array in hex for debugging and final output.
     * @param key
     */
    public static void printColumnBytes(byte[] key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%02X ", key[(4 * j) + i]));
            }
        }
        System.out.println(sb.toString());
    }

    /**
     * Prints bytes in order that they are stored
     * @param key 
     */
    public static void printBytes(byte[] key) {
        StringBuilder sb = new StringBuilder();

        for (byte b : key) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println(sb.toString());
    }

    /**
     * prints bytes in a matrix format
     * @param key 
     */
    public static void printMatrix(byte[] key) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (byte b : key) {
            sb.append(String.format("%02X ", b));
            i++;
            if (i % 4 == 0) {
                sb.append("\n");
            }
        }
        System.out.println(sb.toString());
    }

    /**
     * Prints integers in a matrix format
     * @param key 
     */
    public static void printMatrix(int[] key) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int b : key) {
            sb.append(b + " ");
            i++;
            if (i % 4 == 0) {
                sb.append("\n");
            }
        }
        System.out.println(sb.toString());
    }

    /**
     * Returns sbox matrix given by Qing.
     *
     * I didn't want to look at this big ugly thing so I put it at the bottom.
     *
     * @return
     */
    public static int[] getSBox() {
        /* the S-Box */
        int[] sbox = {0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F,
            0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76, 0xCA, 0x82,
            0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C,
            0xA4, 0x72, 0xC0, 0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC,
            0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15, 0x04, 0xC7, 0x23,
            0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27,
            0xB2, 0x75, 0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52,
            0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84, 0x53, 0xD1, 0x00, 0xED,
            0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58,
            0xCF, 0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9,
            0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8, 0x51, 0xA3, 0x40, 0x8F, 0x92,
            0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
            0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E,
            0x3D, 0x64, 0x5D, 0x19, 0x73, 0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A,
            0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB, 0xE0,
            0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62,
            0x91, 0x95, 0xE4, 0x79, 0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E,
            0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08, 0xBA, 0x78,
            0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B,
            0xBD, 0x8B, 0x8A, 0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E,
            0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E, 0xE1, 0xF8, 0x98,
            0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55,
            0x28, 0xDF, 0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41,
            0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16};
        /* Used in the MixColumns operation */
        return sbox;
    }

}
