import java.net.*;
import java.io.*;
import java.util.Random;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Alice {
    public static void main(String[] args) throws IOException {
        try {
            int port = 8087;

            // Alice Key
            int sk = 3;

            // random number from the cyclic group
            int r = 5;

            // CA digital signature pair of keys
            // Alice Digital certificate
            double ska = 0.00390625;
            double vka = 256;
            // Bob public key from Digital certificate
            double vkb = 148;

            // Client p, g, and key
            double clientP, clientG, BobPK, AlicePK, sharedkey, h;
            String Astr;

            // Roll the dice and get a number
            Random rand = new Random();
            int a = rand.nextInt(7 - 1) + 1;

            // Established the Connection
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            System.out.println("Just connected to " + server.getRemoteSocketAddress());

            
            // Accepts the data from client
            DataInputStream in = new DataInputStream(server.getInputStream());

            clientP = Integer.parseInt(in.readUTF()); // to accept p
            System.out.println("From Bob : P = " + clientP);

            clientG = Integer.parseInt(in.readUTF()); // to accept g
            System.out.println("From Bob : G = " + clientG);

            BobPK = Double.parseDouble(in.readUTF()); // to accept Bob's PK
            System.out.println("From Bob : Public Key ");

            h = Double.parseDouble(in.readUTF()); // to accept h
            System.out.println("From Bob get a public h = gx  ");

            // Sends data to Bob
            // Value of Alice pk
            OutputStream outToclient = server.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToclient);

            AlicePK = ((Math.pow(clientG, sk)) % clientP); // calculation of Alice's pk
            Astr = Double.toString(AlicePK);
            System.out.println("Sending Alice Pk to Bob");
            out.writeUTF(Astr); // Sending Alice pk

            sharedkey = ((Math.pow(BobPK, sk)) % clientP); // calculation of sharedKey
            System.out.println("compute shared key to perform Elgamal Encryption = " + sharedkey);

            // calculate Commitment
            double ga = ((Math.pow(clientG, a)) % clientP); // calculation of g^a
            double hr = ((Math.pow(h, r)) % clientP); // calculation of h^r
            double commitment = ga * hr; // calculation of commitment.
            System.out.println(" Alice computes Commitment C(a, r)");

            // Alice signed the Commitment
            System.out.println("Alice signs the Commitment ");
            double signedC = commitment * ska;

            // Alice Encrypt commitment
            System.out.println("Encrypting the commitment");
            double cypher1 = commitment * sharedkey; // non signed commitment encryption
            double cypher2 = signedC * sharedkey; // signed commitment encryption
            String cypher1str = Double.toString(cypher1);
            String cypher2str = Double.toString(cypher2);

            // Sending cypher1, cypher2
            System.out.println("Alice sends commitment");
            out.writeUTF(cypher1str);
            out.writeUTF(cypher2str);

            // receiving Bob's b

            double cypher1B = Double.parseDouble(in.readUTF()); // to accept Bob's encrypted signed b
            System.out.println("From Bob : Bob's b");
            double cypher2B = Double.parseDouble(in.readUTF()); // to accept Bob's encrypted nonSigned b

            // decrypt Bobs b
            System.out.println("Alice decrypts: Bob's b ");
            double decSigndB = cypher1B / sharedkey;
            double decNonSignedB = cypher2B / sharedkey;

            // Unsign Bob's answer - Digital signature correctnes
            double b = decSigndB * vkb;
            System.out.println("Unsign Bobs b");

            // format Bob's b to 1 decimal number
            //BigDecimal bd = new BigDecimal(b).setScale(1, RoundingMode.HALF_UP);
            //double newBx = bd.doubleValue();
            int newB = (int) Math.round(b);
            if (newB == decNonSignedB) {
                System.out.println("valid Bob's b");
                System.out.println("Bob's b is " + decNonSignedB);
                System.out.println("Alice's a is " + a);
                // round Alice a from double to int
               
                int a_xor_b = (a) ^ newB % 5 + 1;
                System.out.println("Alice and Bob Dice " + a_xor_b);
            } else {
                System.out.println("Invalid Bob's b");
            }

            // knowing her a and Bob's b she can now compute the dice
            // Sign commitment key and claimed a
            double signkey = r * ska;
            double signdice = a * ska;

            // Encrypt her claimed a, signed and nonSigned
            double cypherdice1 = a * sharedkey; // Encrypt Alice's non signeda
            double cypherkey1 = r * sharedkey; // Encrypt the non signed key to open the commitment
            double cypherdice2 = signdice * sharedkey; // Encrypt signed Alice's a
            double cypherkey2 = signkey * sharedkey;// Encrypt the signed key to open the commitment

            // Send the key and Alice claimed a to Bob
            String cypherdice1str = Double.toString(cypherdice1);
            String cypherdice2str = Double.toString(cypherdice2);
            String cypherkey1str = Double.toString(cypherkey1);
            String cypherkey2str = Double.toString(cypherkey2);

            // Sending cypher1, cypher2
            out.writeUTF(cypherdice1str); // sending Alicea
            out.writeUTF(cypherdice2str); // sending signed Alice a
            out.writeUTF(cypherkey1str); // sending Encrypt the key to open the commitment
            out.writeUTF(cypherkey2str); // sending Encrypt the signed key to open the commitment

            server.close();
        }

        catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
        }
    }
}