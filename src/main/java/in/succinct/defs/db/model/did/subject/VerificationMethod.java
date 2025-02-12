package in.succinct.defs.db.model.did.subject;

import com.venky.core.security.Crypt;
import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.ui.HIDDEN;
import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.model.CryptoKey;
import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.documents.Attestation;
import in.succinct.defs.db.model.did.identifier.Did;
import in.succinct.defs.util.KeyManager;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.util.List;

public interface VerificationMethod extends Model, Did {
    @UNIQUE_KEY("K2")
    long getControllerId();
    void setControllerId(long id);
    Subject getController();
    
    
    @Enumeration(enumClass = "in.succinct.defs.db.model.did.subject.VerificationMethod$HashAlgorithm")
    @UNIQUE_KEY(value = "K2", allowMultipleRecordsWithNull = false)
    String getHashingAlgorithm();
    void setHashingAlgorithm(String hashingAlgorithm);
    
    
    
    @Enumeration(enumClass = "in.succinct.defs.db.model.did.subject.VerificationMethod$Purpose")
    @UNIQUE_KEY("K2")
    String getPurpose();
    void setPurpose(String purpose);
    
    enum Purpose {
        Authentication,
        Assertion,
        KeyAgreement,
        CapabilityInvocation,
        CapabilityDelegation,
    }
    
    @Enumeration(enumClass = "in.succinct.defs.db.model.did.subject.VerificationMethod$PublicKeyType")
    @UNIQUE_KEY("K2")
    String getType();
    void setType(String type);
    
    
    /*
        when type isk
        Mail => Email
        Phone => PhoneNumber
        Ed25519,X25519 => corresponding public key
        Dns = > domain name
        
     */
    @UNIQUE_KEY("K2")
    String getPublicKey();
    void setPublicKey(String publicKey);
    
    
    @IS_NULLABLE
    Boolean isVerified();
    void setVerified(Boolean verified);
    
    
    String getChallenge();
    void setChallenge(String challenge);
    
    
    public void challenge();
    public void challenge(boolean save);
    public void verify(String challengeResponse);
    public void verify(String challengeResponse, boolean save);
    
    
    /*
    In case of otp, Challenge and response is same.
     */
    String getResponse();
    void setResponse(String response);
    
    
    enum HashAlgorithm  {
        Blake512("BLAKE2B-512","BLAKE-512");
        
        private final String algoName;
        private final String commonName;
        HashAlgorithm(String algoName, String commonName){
            this.algoName = algoName;
            this.commonName = commonName;
        }
        public String algo(){
            return algoName ;
        }
        public String commonName(){
            return commonName;
        }
        
    }
    enum PublicKeyType {
        
        Ed25519("Ed25519"),
        X25519("X25519") {
            @Override
            public boolean isChallengeEncrypted() {
                return true;
            }
            
            public String encrypt(String challenge, String publicKey){
                    SecretKey symKey = getSecretKey(KeyManager.getInstance().getLatestKey(CryptoKey.PURPOSE_ENCRYPTION).getPrivateKey(),publicKey);
                    return Crypt.getInstance().encrypt(challenge, "AES", symKey);
            }
            private SecretKey getSecretKey(String pv, String pb){
                try {
                    KeyAgreement agreement = KeyAgreement.getInstance(algo());
                    PrivateKey privateKey = Crypt.getInstance().getPrivateKey(algo(), pv);
                    
                    agreement.init(privateKey);
                    agreement.doPhase(Crypt.getInstance().getPublicKey(algo(), pb), true);
                    return agreement.generateSecret("TlsPremasterSecret");
                }catch (Exception ex){
                    throw new RuntimeException(ex);
                }
            }
            public String decrypt(String challenge, String publicKey){
                SecretKey symKey = getSecretKey(KeyManager.getInstance().getLatestKey(CryptoKey.PURPOSE_ENCRYPTION).getPrivateKey(),publicKey);
                return Crypt.getInstance().decrypt(challenge, "AES", symKey);
            }
        },
        Phone {
            @Override
            public boolean isChallengeVerificationInFlight() {
                return true;
            }
        },
        Email {
            @Override
            public boolean isChallengeVerificationInFlight() {
                return true;
            }
        },
        Dns;
        private final String algoName;
        private final String commonName;
        
        
        PublicKeyType(){
            this(null);
        }
        PublicKeyType(String algoName){
            this(algoName,algoName == null ? null : algoName.toLowerCase());
        }
        PublicKeyType(String algoName, String commonName){
            this.algoName = algoName;
            this.commonName = commonName;
        }
        public String algo(){
            return algoName == null ? name() : algoName;
        }
        public String commonName(){
            return commonName == null ? name().toLowerCase() : commonName;
        }
        
        public boolean isChallengeVerificationInFlight(){
            return false;
        }
        
        public boolean isChallengeEncrypted(){
            return false;
        }
     
        public String encrypt(String payload, String publicKey){
            return payload;
        }
        public String decrypt(String payload, String publicKey){
            return payload;
        }
        
    }
    
    @HIDDEN
    List<Attestation> getSignatures();
    
    
}
