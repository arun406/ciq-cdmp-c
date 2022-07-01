package com.aktimetrix.service.notification.notification;

/**
 * @author Arun.Kandakatla
 */

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class Address implements Serializable {

    /**
     * This Address type defines that this AddressDTO represents a Paritcipant
     */
    public static final int ADDRESSTYPE_FTP = 1;

    /**
     * This Address type defines that this AddressDTO represents a Email Address
     */
    public static final int ADDRESSTYPE_EMAIL = 2;

    /**
     * This Address type defines that this AddressDTO represents a SMS Address
     */
    public static final int ADDRESSTYPE_SMS = 3;

    /**
     * This Address type defines that this AddressDTO represents a SITA Address
     */
    public static final int ADDRESSTYPE_SITA = 4;

    /**
     * This Address type defines that this AddressDTO represents a JMS Address
     */
    public static final int ADDRESSTYPE_JMS = 6;

    /**
     * This Address type defines that this AddressDTO represents a FAX Address
     */
    public static final int ADDRESSTYPE_FAX = 10;

    /**
     * This Address type defines that this AddressDTO represents a MQ Address
     */
    public static final int ADDRESSTYPE_MQ = 11;

    /**
     * This Address type defines that this AddressDTO represents a Paritcipant
     */
    public static final int ADDRESSTYPE_PARTICIPANT = 12;

    /**
     * This Address type defines that this AddressDTO represents a Customer
     */
    public static final int ADDRESSTYPE_CUSTOMER = 13;

    /**
     * This Address type defines that this AddressDTO represents a Group
     */
    public static final int ADDRESSTYPE_GROUP = 14;

    /**
     * This Address type defines that this AddressDTO represents a Airport
     */
    public static final int ADDRESSTYPE_AIRPORT = 15;

    /**
     * This Address type defines that this AddressDTO represents a Station
     */
    public static final int ADDRESSTYPE_STATION = 16;

    /**
     * This Address type defines that this AddressDTO represents a City
     */
    public static final int ADDRESSTYPE_CITY = 17;

    /**
     * This Address type defines that this AddressDTO represents a Airline Code
     */
    public static final int ADDRESSTYPE_AIRLINE = 18;

    /**
     * Address Type
     **/
    private int addressType;
    /**
     * Address Identifier
     **/
    private String addressIdentifier;
    /**
     * Participant Type
     */
    private String participantType;
    /**
     * List of copy addresses (Optional)
     **/
    private List<Address> copyAddresses;

    private String messageType;
    /**
     * participant type
     */
    private String type;

    private String senderAddressType;

    public Address() {
    }

    public Address(int addressType, String addressIdentifier) {
        this(addressType, addressIdentifier, null);
    }

    public Address(int addressType, String addressIdentifier,
                   String participantType) {
        this.addressType = addressType;
        this.addressIdentifier = addressIdentifier;
        this.participantType = participantType;
    }


    /**
     * add one more address as copy address
     **/
    public void addCopyAddresses(int addressType, String addressIdentifier) {
        if (copyAddresses == null) {
            copyAddresses = new ArrayList();
        }
        Address address = new Address(addressType, addressIdentifier);
        copyAddresses.add(address);
    }


    /**
     * add one more address as copy address
     **/
    public void addCopyAddresses(Address address) {
        if (copyAddresses == null) {
            copyAddresses = new ArrayList();
        }

        copyAddresses.add(address);
    }

    /**
     * add one more address as copy address
     **/
    public void addCopyAddresses(int addressType, String addressIdentifier,
                                 String participantType) {
        if (copyAddresses == null) {
            copyAddresses = new ArrayList();
        }
        Address address = new Address(
                addressType, addressIdentifier, participantType);
        copyAddresses.add(address);
    }

}
