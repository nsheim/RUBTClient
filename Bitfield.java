
import java.util.BitSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Bitfield.java
 * 
 * Keeps track of and manages information related to bitfield values.
 * 
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 * 
 */

public class Bitfield{
    private int numPieces;
    
    //BitSet object to represent a bitfield
    private BitSet bitfield;
    
    /**
     * Constructor for objects of class Bitfield.
     * @param numPieces current number of pieces the client has
     */
    public Bitfield(int numPieces){
        this.numPieces = numPieces;
        bitfield = new BitSet();
        bitfield.set(numPieces,true); //bitfield[numPieces] = true -- makes sure bitfield is of proper size
    }
    public Bitfield(int numPieces, byte[] bitfieldBytes){
        this.numPieces = numPieces;
        BitSet temp = BitSet.valueOf(bitfieldBytes);
        
        for (int i = 0; i < temp.size()/8; i++) {
            swapBits(temp, i, i+7);
            swapBits(temp, i+1, i+6);
            swapBits(temp, i+2, i+5);
            swapBits(temp, i+3, i+4);
        }
        bitfield = temp;
        
    }
    /**
     * Returns the value at [index] in the bitfield.
     * @return bitfield.get(index)
     */
    public boolean get(int index){
        return bitfield.get(index);
    }
    /**
     * Sets the value at bitfield.get(index) to value.
     * @return true if the index was valid, else return false
     */
    public boolean set(int index, boolean value){
        if(index<numPieces && index>=0){
            bitfield.set(index, value);
            return true;
        }
        return false;
    }
    
    /**
     * Converts the BitSet into a boolean array.
     * @return the boolean array representing the BitSet bitfield
     */
    public boolean[] toBooleanArray(){
        boolean[] bitfieldBooleans = new boolean[numPieces];
        for (int i = 0; i<numPieces;i++){
            bitfieldBooleans[i] = bitfield.get(i);
        }
        return bitfieldBooleans;
    }
    
    /**
     * Converts the BitSet into a byte array.
     * @return the byte array representing the BitSet bitfield
     */
    public byte[] toByteArray(){
        byte[] bitfieldBytes;
        RUBTClient.debugPrint("bitfield length: "+bitfield.length());
        if ((bitfield.length()-1)%8==0){
            bitfieldBytes = new byte[(bitfield.length()-1)/8];
        }
        else{
            bitfieldBytes = new byte[((bitfield.length()-1)/8)+1];
        }
        //RUBTClient.debugPrint("bitfield bytes: " + bitfieldBytes.length);
        for (int i = 0; i<bitfieldBytes.length;i++){
            byte temp = 0;
            for (int j = 0; j<8 && (i*8+j)<(bitfield.length()-1);j++){
                //RUBTClient.debugPrint("i: "  + i + ", j: " + j );
                //RUBTClient.debugPrint("index: " + (i*8+j) + " -- " + bitfield.get(i*8+j));

                if (bitfield.get(i*8+j)){
                    //RUBTClient.debugPrint("index: " + (i*8+j) + " -- " + bitfield.get(i*8+j));
                    temp+=Math.pow(2, j);
                }
            }
            bitfieldBytes[i] = temp;
        }
        return bitfieldBytes;
    }
    /**
     * Converts a boolean bitfield array into a byte array.
     * @return the boolean array representing booleanBitfield
     */
    public static byte[] toByteArray(boolean[] booleanBitfield){
        byte[] bitfieldBytes;
        RUBTClient.debugPrint("bitfield length: "+ booleanBitfield.length);
        if ((booleanBitfield.length-1)%8==0){
            bitfieldBytes = new byte[(booleanBitfield.length-1)/8];
        }
        else{
            bitfieldBytes = new byte[((booleanBitfield.length-1)/8)+1];
        }
        //RUBTClient.debugPrint("bitfield bytes: " + bitfieldBytes.length);
        for (int i = 0; i<bitfieldBytes.length;i++){
            byte temp = 0;
            for (int j = 0; j<8 && (i*8+j)<(booleanBitfield.length-1);j++){
                //RUBTClient.debugPrint("i: "  + i + ", j: " + j );
                //RUBTClient.debugPrint("index: " + (i*8+j) + " -- " + bitfield.get(i*8+j));

                if (booleanBitfield[i*8+j]){
                    //RUBTClient.debugPrint("index: " + (i*8+j) + " -- " + bitfield.get(i*8+j));
                    temp+=Math.pow(2, j);
                }
            }
            bitfieldBytes[i] = temp;
        }
        return bitfieldBytes;
    }
    /**
     * Returns the length of the bitfield.
     * The length of the bitfield is bitfield.length()-1 because one extra value is added to the length
     * as a placeholder (i.e., to make sure the length of the bitfield matches the number of pieces).
     * @return bitfield.length()-1
     */
    public int length(){
        return bitfield.length()-1;
    }
    /**
     * Overrides the toString() method in the Object class.
     */
    @Override
    public String toString(){
        String str = "[";
        for (int i = 0; i<bitfield.length()-1;i++){
            if(bitfield.get(i)) str+="("+i+" - 1),";
            else str+="("+i+" - 0),";
        }
        if (bitfield.get(bitfield.length()-1)) str+=""+(bitfield.length()-1)+" - 1]";
        else str+=""+(bitfield.length()-1)+" - 0]";
        return str;
    }
    
    /**
     * Swaps the values of two bits in bitset.
     * @param bitset the bitset in which the values will be swapped
     * @param a the index of the first bit whose value is to be swapped
     * @param b the index of the second bit whose value is to be swapped
     */
    private static void swapBits(BitSet bitset, int a, int b) {
        boolean tmp = bitset.get(a);
        bitset.set(a, bitset.get(b));
        bitset.set(b, tmp);
    }
}
