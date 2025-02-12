package in.succinct.defs.db.model.did.beckn.bc;

import in.succinct.beckn.BecknObject;
import in.succinct.beckn.BecknObjects;
import in.succinct.beckn.Location;
import in.succinct.beckn.Organization;
import in.succinct.beckn.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Date;

public class Subscriber extends BecknObject {
    public static final String SUBSCRIBER_STATUS_SUBSCRIBED = "SUBSCRIBED";
    public static final String SUBSCRIBER_STATUS_INITIATED = "INITIATED";

    public Subscriber() {
    }

    public Subscriber(String payload) {
        super(payload);
    }

    public Subscriber(JSONObject object) {
        super(object);
    }


    public String getCountry(){
        return get("country");
    }
    public void setCountry(String country){
        set("country",country);
    }
    public String getCity(){
        return get("city");
    }
    public void setCity(String city){
        set("city",city);
    }

    public String getDomain(){
        return get("domain");
    }
    public void setDomain(String domain){
        set("domain",domain);
    }
    public String getStatus(){
        return get("status");
    }
    public void setStatus(String status){
        set("status",status);
    }

    public String getType(){
        return get("type");
    }
    public void setType(String type){
        set("type",type);
    }

    public String getSubscriberId(){
        return get("subscriber_id");
    }
    public void setSubscriberId(String subscriber_id){
        set("subscriber_id",subscriber_id);
    }

    public String getSubscriberUrl(){
        return get("subscriber_url");
    }
    public void setSubscriberUrl(String subscriber_url){
        set("subscriber_url",subscriber_url);
    }

    public String getUniqueKeyId(){
        return get("unique_key_id");
    }
    public void setUniqueKeyId(String unique_key_id){
        set("unique_key_id",unique_key_id);
    }

    public String getPubKeyId(){
        return getUniqueKeyId();
    }
    public void setPubKeyId(String pub_key_id){
        setUniqueKeyId(pub_key_id);
    }

    public String getSigningPublicKey(){
        return get("signing_public_key");
    }
    public void setSigningPublicKey(String signing_public_key){
        set("signing_public_key",signing_public_key);
    }
    public String getEncrPublicKey(){
        return get("encr_public_key");
    }
    public void setEncrPublicKey(String encr_public_key){
        set("encr_public_key",encr_public_key);
    }

    public String getNonce(){
        return get("nonce");
    }
    public void setNonce(String nonce){
        set("nonce",nonce);
    }

    public Date getValidFrom(){
        return getTimestamp("valid_from");
    }
    public void setValidFrom(Date valid_from){
        set("valid_from",valid_from,TIMESTAMP_FORMAT);
    }
    public Date getValidTo(){
        return getTimestamp("valid_until");
    }
    public void setValidTo(Date valid_to){
        set("valid_until",valid_to,TIMESTAMP_FORMAT);
    }

    public Date getCreated(){
        return getTimestamp("created");
    }
    public void setCreated(Date created){
        set("created",created,TIMESTAMP_FORMAT);
    }
    public Date getUpdated(){
        return getTimestamp("updated");
    }
    public void setUpdated(Date updated){
        set("updated",updated,TIMESTAMP_FORMAT);
    }


    public Location getLocation(){
        return get(Location.class, "location");
    }
    public void setLocation(Location location){
        set("location",location);
    }

    public boolean isExtendedAttributesDisplayed(){
        return true;
    }
    public String getAlias(){
        return extendedAttributes.get("alias",getUniqueKeyId());
    }
    public void setAlias(String alias){
        extendedAttributes.set("alias",alias);
    }

    public Organization getOrganization(){
        return extendedAttributes.get(Organization.class, "organization");
    }
    public void setOrganization(Organization organization){
        extendedAttributes.set("organization",organization);
    }

    @SuppressWarnings("unchecked")
    public JSONObject getInner(boolean includeExtended) {
        JSONObject inner = new JSONObject();
        inner.putAll(super.getInner());
        if (!includeExtended){
            inner.remove("extended_attributes");
        }
        return inner;
    }

    public Request getSubscribeRequest(){
        return extendedAttributes.get(Request.class, "request");
    }
    public void setSubscribeRequest(Request request){
        extendedAttributes.set("request",request);
    }

    public boolean isMsn(){
        return extendedAttributes.getBoolean("msn");
    }
    public void setMsn(boolean msn){
        extendedAttributes.set("msn",msn);
    }


    public Domains getDomains(){
        return extendedAttributes.get(Domains.class, "domains");
    }
    public void setDomains(Domains domains){
        extendedAttributes.set("domains",domains);
    }


    public static class Domains extends BecknObjects<String> {
        public Domains() {
        }

        public Domains(JSONArray value) {
            super(value);
        }

        public Domains(String payload) {
            super(payload);
        }
    }


}
