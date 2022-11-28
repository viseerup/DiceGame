import java.net.*;
import java.io.*;
import java.util.Random;
import java.lang.Math;

  
public class Bob  {
    public static void main(String[] args)
    {
        try {
            String pstr, gstr, Bstr;
            String serverName = "localhost";
            int port = 8087;
  
            // Declare p, g, and Key of client
            int p = 11;
            int g = 8;
            int sk = 5;
            int x = 3;
            double sharedKey, AlicePk, h;
            
            // CA digital signature pair of keys
            //Alice Digital certificate
            double skb = 0.006756757;
            double vkb = 148;
            //Bob public key from Digital certificate
            double vka = 256;


           // Roll the dice and get a number
           Random rand = new Random();
           int b = rand.nextInt(7-1) + 1;

            // Established the connection
            System.out.println("Connecting to " + serverName
                               + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to "
                               + client.getRemoteSocketAddress());
  
            // Sends the data to client
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
  
            pstr = Integer.toString(p);
            out.writeUTF(pstr); // Sending p
  
            gstr = Integer.toString(g);
            out.writeUTF(gstr); // Sending g
  
            double B = ((Math.pow(g, sk)) % p); // calculation of Bob pk
            Bstr = Double.toString(B);
            out.writeUTF(Bstr); // Sending Bob pk

            h = ((Math.pow(g, x)) % p); // calculation of g^x
            String Hstr = Double.toString(h);
            out.writeUTF(Hstr); // Sending h 
  
            // Accepts the data
            DataInputStream in = new DataInputStream(client.getInputStream());
  
            AlicePk = Double.parseDouble(in.readUTF());
            System.out.println("From Alice : Public Key ");
  
            sharedKey = ((Math.pow(AlicePk, sk)) % p); // calculation of sharedkey
  
            System.out.println("compute shared key");

            // Get cypher text with the commitments both signed and unsigned
            double cypher1 = Double.parseDouble(in.readUTF());
            System.out.println("From Alice : NonSigned commitment");

            double cypher2 = Double.parseDouble(in.readUTF());
            System.out.println("From Alice : Signed commitment ");

            // decrypt cypher  texts with the commitments

            double c1 = cypher1 / sharedKey;
            double c2 = cypher2 / sharedKey;

            // Digital signature correctness for the signed commitment
            double commitment1 = c2 * vka;
            System.out.println("Unsign Alice's commitment");

            if (commitment1 == c1){
                System.out.println("valid signed commitment");

            }
            else {
                System.out.println("Invalid signed commitment");
            }

            // sign and encrypt b
            double signdice = b * skb;
            
            // Encrypte signed b
            double encSigndice = signdice * sharedKey;

            // Encrypte non signed b
            double encdice = b * sharedKey;
           
            // send dice answers to Alice
            String signDicestr = Double.toString(encSigndice);
            out.writeUTF(signDicestr); // Sending signed a to Alice
            
            String dicestr = Double.toString(encdice);
            out.writeUTF(dicestr); // Sending non signed a to Alice
           
            // receiving the key and Alice's a
            double cypherdice1 = Double.parseDouble(in.readUTF());
            System.out.println("From Alice : encrypted a ");

            double cypherdice2 = Double.parseDouble(in.readUTF());
            System.out.println("From Alice : encrypted Signed a ");

            double cypherkey1 = Double.parseDouble(in.readUTF());
            System.out.println("From Alice : encrypted commitment key ");

            double cypherkey2 = Double.parseDouble(in.readUTF());
            System.out.println("From Alice : encrypted Signed commitment key ");

            //Decrypt a and the key
            double decCypher1 = cypherdice1 / sharedKey;
            double decCypher2 = cypherdice2 / sharedKey;
            double decCypherKey1 = cypherkey1 / sharedKey;
            double decCypherKey2 = cypherkey2 / sharedKey;

           // Digital signature correctness for the signed a  and key
           double a = decCypher2 * vka;
           System.out.println("Unsign Alice's a");

            // check integrity and authenticity
           if (a == decCypher1){
               System.out.println("valid signed a");

           }
           else {
               System.out.println("Invalid signed a");
           }


           // Digital signature correctness for the signed key to open commitment
           double commitmentKey = decCypherKey2 * vka;
           System.out.println("Unsign key to open commitment");

            // check integrity and authenticity of the commitment key
           if (commitmentKey == decCypherKey1){
               System.out.println("valid signed key");

           }
           else {
               System.out.println("Invalid signed key");
           }

           // Bob computes alice's commitment with the key and message he got
           
             double ga= ((Math.pow(g, decCypher1)) % p); // calculation of g^a
             double hr= ((Math.pow(h, decCypherKey1)) % p); // calculation of h^r
             double testCommitment = ga * hr; // calculation of commitment.
             System.out.println("Computes alice's commitment with the key and a sent by Alice");

           // Open fase Bob compares the computed commitment with the one sent by alice erliar
           System.out.println("Does Alice's claimed answer much her commitment? : ");
           if(c1 == testCommitment){
               System.out.println(" True ");
               System.out.println(" Openned commitment with the key ");
               //round Alice a from double to int
               int newA = (int)Math.round(a);
               int a_xor_b = (newA)^b % 5 + 1;
               System.out.println("Alice and Bob Dice "+ a_xor_b);
              

           }
           else {
               System.out.println("False");
               System.out.println("Discard the commitment");
           }
 
         





            

        








            client.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}