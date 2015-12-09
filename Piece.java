
/**
 * Write a description of class Piece here.
 *  @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353
 */
public class Piece
{
   private int index; //index of the piece
   
   private int pieceLength;
   private byte[] data;
   
   private boolean verified;
   
   private int rarity; // rarity = # occurrences, so the lower rarity is, the more rare the piece is
   
   public Piece(int index, int pieceLength){
       this.index = index;
       this.pieceLength = pieceLength;
       data = new byte[pieceLength];
       verified = false;
       
       rarity = 0;
   }
   
   public int getIndex(){
       return index;
   }
   
   public int length(){
       return pieceLength;
   }
   
   public byte[] getData(){
        return data;
   }
   public byte getDataByte(int index){
       return data[index];
   }
   public void setData(int index, byte dataByte){
       data[index] = dataByte;
   }
   
   public void setVerified(boolean verified){
       this.verified = verified;
   }
   public boolean isVerified(){
       return verified;
   }
   
   public void setRarity(int rarity){
       this.rarity = rarity;
   }
   public int getRarity(){
       
        return rarity;
   }
   public void incrementRarity(){
       rarity++;
   }
   
   @Override
   public boolean equals (Object obj){
       if (obj == null) return false;
       if (obj == this) return true;
       if (!(obj.getClass() != getClass()))return false;
       Piece pieceObj = (Piece) obj;
       return index == pieceObj.getIndex();
   }
   
   @Override
   public int hashCode(){
       return index;
   }
}
